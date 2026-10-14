package com.ehealthwares.rxsoft.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.StockBalanceDto
import com.ehealthwares.rxsoft.data.remote.dto.StockLocationDto
import com.ehealthwares.rxsoft.data.repository.InventoryRepository
import com.ehealthwares.rxsoft.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockBalanceViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _stockBalances = MutableStateFlow<UiState<List<StockBalanceDto>>>(UiState.Loading)
    val stockBalances: StateFlow<UiState<List<StockBalanceDto>>> = _stockBalances.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    /** Stock locations for the location filter chips (failure-tolerant). */
    private val _locations = MutableStateFlow<List<StockLocationDto>>(emptyList())
    val locations: StateFlow<List<StockLocationDto>> = _locations.asStateFlow()

    private var currentPage = 1
    private var hasMore = true
    private var currentSearch: String? = null
    private var currentCategoryCode: String? = null
    private var currentLocationId: String? = null

    init {
        loadStockBalances()
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            inventoryRepository.listStockLocations()
                .onSuccess { _locations.value = it }
                .onFailure { e -> Log.w("StockBalanceVM", "Locations unavailable: ${e.message}") }
        }
    }

    fun loadStockBalances(
        search: String? = null,
        categoryCode: String? = null,
        locationId: String? = null,
    ) {
        currentSearch = search
        currentCategoryCode = categoryCode
        currentLocationId = locationId
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _stockBalances.value = UiState.Loading
            inventoryRepository.getStockBalances(search = search, page = 1, categoryCode = categoryCode, locationId = locationId)
                .onSuccess {
                    _stockBalances.value = UiState.Success(it)
                    hasMore = it.size >= 100
                }
                .onFailure { e ->
                    Log.e("StockBalanceVM", "Failed to load stock: ${e.message}", e)
                    _stockBalances.value = UiState.Error(e.message ?: "Failed to load stock")
                }
        }
    }

    fun loadMoreStockBalances() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            inventoryRepository.getStockBalances(
                search = currentSearch,
                page = currentPage,
                categoryCode = currentCategoryCode,
                locationId = currentLocationId,
            )
                .onSuccess { items ->
                    val current = (_stockBalances.value as? UiState.Success)?.data ?: emptyList()
                    _stockBalances.value = UiState.Success(current + items)
                    hasMore = items.size >= 100
                }
                .onFailure { e ->
                    Log.e("StockBalanceVM", "Failed to load more stock: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
