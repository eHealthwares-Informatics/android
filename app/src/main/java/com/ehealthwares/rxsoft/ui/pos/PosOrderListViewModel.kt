package com.rxsoft.mobile.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PosOrderListViewModel @Inject constructor(
    private val posRepository: PosRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<UiState<List<SaleDto>>>(UiState.Loading)
    val sales: StateFlow<UiState<List<SaleDto>>> = _sales.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var hasMore = true

    init {
        loadSales()
    }

    fun loadSales() {
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _sales.value = UiState.Loading
            posRepository.listSales(page = 1)
                .onSuccess {
                    _sales.value = UiState.Success(it)
                    hasMore = it.size >= 20
                }
                .onFailure { e ->
                    Log.e("PosOrderListVM", "Failed to load sales: ${e.message}", e)
                    _sales.value = UiState.Error(e.message ?: "Failed to load sales")
                }
        }
    }

    fun loadMoreSales() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            posRepository.listSales(page = currentPage)
                .onSuccess { items ->
                    val current = (_sales.value as? UiState.Success)?.data ?: emptyList()
                    _sales.value = UiState.Success(current + items)
                    hasMore = items.size >= 20
                }
                .onFailure { e ->
                    Log.e("PosOrderListVM", "Failed to load more sales: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
