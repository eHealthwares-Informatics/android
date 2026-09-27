package com.rxsoft.mobile.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.StockBalanceDto
import com.rxsoft.mobile.data.repository.InventoryRepository
import com.rxsoft.mobile.util.UiState
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

    private var currentPage = 1
    private var hasMore = true
    private var currentSearch: String? = null
    private var currentCategoryCode: String? = null

    init {
        loadStockBalances()
    }

    fun loadStockBalances(search: String? = null, categoryCode: String? = null) {
        currentSearch = search
        currentCategoryCode = categoryCode
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _stockBalances.value = UiState.Loading
            inventoryRepository.getStockBalances(search = search, page = 1, categoryCode = categoryCode)
                .onSuccess {
                    _stockBalances.value = UiState.Success(it)
                    hasMore = it.size >= 20
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
            inventoryRepository.getStockBalances(search = currentSearch, page = currentPage, categoryCode = currentCategoryCode)
                .onSuccess { items ->
                    val current = (_stockBalances.value as? UiState.Success)?.data ?: emptyList()
                    _stockBalances.value = UiState.Success(current + items)
                    hasMore = items.size >= 20
                }
                .onFailure { e ->
                    Log.e("StockBalanceVM", "Failed to load more stock: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
