package com.rxsoft.mobile.ui.customers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.CustomerDto
import com.rxsoft.mobile.data.repository.CustomerRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<UiState<List<CustomerDto>>>(UiState.Loading)
    val customers: StateFlow<UiState<List<CustomerDto>>> = _customers.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var hasMore = true
    private var currentSearch: String? = null

    init {
        loadCustomers()
    }

    fun loadCustomers(search: String? = null) {
        currentSearch = search
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _customers.value = UiState.Loading
            customerRepository.listCustomers(search = search, page = 1)
                .onSuccess {
                    _customers.value = UiState.Success(it)
                    hasMore = it.size >= 20
                }
                .onFailure { e ->
                    Log.e("CustomerListVM", "Failed to load customers: ${e.message}", e)
                    _customers.value = UiState.Error(e.message ?: "Failed to load customers")
                }
        }
    }

    fun loadMoreCustomers() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            customerRepository.listCustomers(search = currentSearch, page = currentPage)
                .onSuccess { items ->
                    val current = (_customers.value as? UiState.Success)?.data ?: emptyList()
                    _customers.value = UiState.Success(current + items)
                    hasMore = items.size >= 20
                }
                .onFailure { e ->
                    Log.e("CustomerListVM", "Failed to load more customers: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
