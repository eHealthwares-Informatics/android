package com.ehealthwares.rxsoft.ui.pos

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.api.PrintApi
import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintItem
import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintRequest
import com.ehealthwares.rxsoft.data.remote.dto.SaleDto
import com.ehealthwares.rxsoft.data.repository.PosRepository
import com.ehealthwares.rxsoft.util.PrinterUrlManager
import com.ehealthwares.rxsoft.util.ReceiptData
import com.ehealthwares.rxsoft.util.ReceiptLine
import com.ehealthwares.rxsoft.util.UiState
import com.ehealthwares.rxsoft.util.printReceipt
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/** Outcome of a reprint request from the order detail screen. */
sealed interface ReprintState {
    data object Idle : ReprintState
    data object Printing : ReprintState
    data class Success(val message: String) : ReprintState
    data class Failure(val message: String) : ReprintState
}

@HiltViewModel
class PosOrderDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val posRepository: PosRepository,
    private val printApi: PrintApi,
    private val printerUrlManager: PrinterUrlManager,
) : ViewModel() {

    private val _sale = MutableStateFlow<UiState<SaleDto>>(UiState.Idle)
    val sale: StateFlow<UiState<SaleDto>> = _sale.asStateFlow()

    private val _reprint = MutableStateFlow<ReprintState>(ReprintState.Idle)
    val reprint: StateFlow<ReprintState> = _reprint.asStateFlow()

    fun loadSale(id: String) {
        viewModelScope.launch {
            _sale.value = UiState.Loading
            posRepository.getSale(id)
                .onSuccess { _sale.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load sale: ${e.message}", e)
                    _sale.value = UiState.Error(e.message ?: "Failed to load sale")
                }
        }
    }

    /** Reprint the currently loaded sale to the configured print-agent, with a
     *  system-print/PDF fallback when the agent is unreachable. */
    fun printSale() {
        val current = _sale.value
        if (current !is UiState.Success<*>) {
            _reprint.value = ReprintState.Failure("Sale not loaded yet")
            return
        }
        @Suppress("UNCHECKED_CAST")
        val sale = current.data as SaleDto

        if (_reprint.value is ReprintState.Printing) return
        viewModelScope.launch {
            _reprint.value = ReprintState.Printing

            val lines = sale.lines?.map { line ->
                ReceiptPrintItem(
                    name = line.item?.name ?: "Item",
                    qty = line.quantity.toDouble(),
                    price = line.unitPrice.toDouble(),
                    total = line.lineTotal.toDouble(),
                )
            } ?: emptyList()

            val request = ReceiptPrintRequest(
                saleNumber = sale.saleNumber,
                items = lines,
                subtotal = lines.sumOf { it.total },
                total = sale.totalAmount.toDouble(),
                paidAmount = sale.paidAmount.toDouble(),
            )

            val base = printerUrlManager.getUrl().trimEnd('/')
            val url = "$base/print/receipt"
            Log.i(TAG, "Reprint sale ${sale.saleNumber} -> $url")

            runCatching { printApi.printReceipt(url, request) }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        _reprint.value = ReprintState.Success("Reprinted ${sale.saleNumber}")
                    } else {
                        reprintLocally(sale, lines, "Agent HTTP ${resp.code()}")
                    }
                }
                .onFailure { e ->
                    Log.w(TAG, "Print service unavailable, using system print", e)
                    reprintLocally(sale, lines, "Agent unreachable")
                }
        }
    }

    private fun reprintLocally(sale: SaleDto, lines: List<ReceiptPrintItem>, reason: String) {
        try {
            val receiptData = ReceiptData(
                saleNumber = sale.saleNumber,
                customerName = sale.customer?.name,
                items = lines.map {
                    ReceiptLine(
                        it.name,
                        it.qty.toBigDecimal(),
                        it.price.toBigDecimal(),
                        it.total.toBigDecimal(),
                    )
                },
                subtotal = sale.totalAmount,
                total = sale.totalAmount,
                paidAmount = sale.paidAmount,
                changeAmount = BigDecimal.ZERO,
            )
            printReceipt(context, receiptData)
            _reprint.value = ReprintState.Success("$reason — using system print")
        } catch (e: Exception) {
            Log.e(TAG, "Fallback print failed", e)
            _reprint.value = ReprintState.Failure("Could not print: ${e.message ?: "unknown error"}")
        }
    }

    fun resetReprint() {
        _reprint.value = ReprintState.Idle
    }

    companion object {
        private const val TAG = "PosOrderDetailVM"
    }
}
