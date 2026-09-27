package com.rxsoft.mobile.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.AdjustStockRequest
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.remote.dto.StockBalanceDto
import com.rxsoft.mobile.data.repository.InventoryRepository
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.PosConfigManager
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class StockAdjustmentViewModel @Inject constructor(
    private val posRepository: PosRepository,
    private val inventoryRepository: InventoryRepository,
    private val posConfigManager: PosConfigManager
) : ViewModel() {

    private var searchJob: Job? = null

    private val _searchResults = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<ItemDto>>> = _searchResults.asStateFlow()

    private val _selectedItem = MutableStateFlow<ItemDto?>(null)
    val selectedItem: StateFlow<ItemDto?> = _selectedItem.asStateFlow()

    private val _deltaQuantity = MutableStateFlow(BigDecimal.ZERO)
    val deltaQuantity: StateFlow<BigDecimal> = _deltaQuantity.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<StockBalanceDto>>(UiState.Idle)
    val submitState: StateFlow<UiState<StockBalanceDto>> = _submitState.asStateFlow()

    private val userPosConfig
        get() = posConfigManager.config.value

    init {
        posConfigManager.loadConfig()
    }

    fun searchItems(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = UiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            _searchResults.value = UiState.Loading
            posRepository.searchOrgItems(query)
                .onSuccess { _searchResults.value = UiState.Success(it) }
                .onFailure { _searchResults.value = UiState.Error(it.message ?: "Search failed") }
        }
    }

    fun selectItem(item: ItemDto) {
        _selectedItem.value = item
        _searchResults.value = UiState.Idle
    }

    fun updateDeltaQuantity(value: String) {
        _deltaQuantity.value = value.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    fun updateReason(value: String) {
        _reason.value = value
    }

    fun submit() {
        val item = _selectedItem.value ?: run {
            Log.e("StockAdjustVM", "Submit failed: no item selected")
            _submitState.value = UiState.Error("Select an item")
            return
        }
        val locationId = userPosConfig?.stockLocation?.id ?: run {
            Log.e("StockAdjustVM", "Submit failed: stock location not configured")
            _submitState.value = UiState.Error("Stock location not configured")
            return
        }
        if (_reason.value.isBlank()) {
            Log.e("StockAdjustVM", "Submit failed: no reason provided")
            _submitState.value = UiState.Error("Enter a reason")
            return
        }
        if (_deltaQuantity.value.compareTo(BigDecimal.ZERO) == 0) {
            Log.e("StockAdjustVM", "Submit failed: quantity is zero")
            _submitState.value = UiState.Error("Quantity cannot be zero")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            val request = AdjustStockRequest(
                itemId = item.id,
                locationId = locationId,
                deltaQuantity = _deltaQuantity.value,
                reason = _reason.value
            )
            inventoryRepository.adjustStock(request)
                .onSuccess { balance ->
                    Log.d("StockAdjustVM", "Adjustment applied: ${balance.quantityOnHand}")
                    _submitState.value = UiState.Success(balance)
                }
                .onFailure { e ->
                    Log.e("StockAdjustVM", "Adjustment failed: ${e.message}", e)
                    _submitState.value = UiState.Error(e.message ?: "Adjustment failed")
                }
        }
    }

    fun reset() {
        _selectedItem.value = null
        _deltaQuantity.value = BigDecimal.ZERO
        _reason.value = ""
        _submitState.value = UiState.Idle
        _searchResults.value = UiState.Idle
    }

    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }
}
