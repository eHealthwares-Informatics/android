package com.rxsoft.mobile.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.data.remote.dto.SaleLineDto
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One sale line flattened from a POS sale, prefixed with its parent sale context. */
data class SaleLineRow(
    val saleId: String,
    val lineId: String?,
    val lineNumber: Int,
    val saleNumber: String,
    val saleChannel: String,
    val saleDate: String,
    val status: String,
    val itemName: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
)

/** Sales Lines screen: every line item across POS sales. */
@HiltViewModel
class SaleLinesViewModel @Inject constructor(
    private val posRepository: PosRepository,
) : ViewModel() {

    private val _lines = MutableStateFlow<UiState<List<SaleLineRow>>>(UiState.Loading)
    val lines: StateFlow<UiState<List<SaleLineRow>>> = _lines.asStateFlow()

    init {
        loadLines()
    }

    fun loadLines() {
        viewModelScope.launch {
            _lines.value = UiState.Loading
            posRepository.listSales()
                .map { sales -> flattenSaleLines(sales) }
                .onSuccess { _lines.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("SaleLinesVM", "Failed to load sale lines: ${e.message}", e)
                    _lines.value = UiState.Error(e.message ?: "Failed to load sale lines")
                }
        }
    }

    private fun flattenSaleLines(sales: List<SaleDto>): List<SaleLineRow> {
        return sales.flatMap { sale ->
            sale.lines.orEmpty().map { line ->
                SaleLineRow(
                    saleId = sale.id,
                    lineId = line.id,
                    lineNumber = line.lineNumber,
                    saleNumber = sale.saleNumber,
                    saleChannel = sale.saleChannel,
                    saleDate = sale.saleDate,
                    status = sale.status,
                    itemName = saleLineLabel(line),
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                    lineTotal = line.lineTotal,
                )
            }
        }
    }
}

private fun saleLineLabel(line: SaleLineDto): String {
    line.item?.name?.let { return it }
    line.item?.code?.let { return it }
    return "Line #${line.lineNumber}"
}