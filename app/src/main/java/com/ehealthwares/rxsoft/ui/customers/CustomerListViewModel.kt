package com.ehealthwares.rxsoft.ui.customers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.CreateCustomerRequest
import com.ehealthwares.rxsoft.data.remote.dto.CustomerDto
import com.ehealthwares.rxsoft.data.repository.CustomerRepository
import com.ehealthwares.rxsoft.util.UiState
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

    /** Create-customer flow state (FAB → dialog). */
    private val _createState = MutableStateFlow<UiState<CustomerDto>>(UiState.Idle)
    val createState: StateFlow<UiState<CustomerDto>> = _createState.asStateFlow()

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
                    hasMore = it.size >= 100
                }
                .onFailure { e ->
                    Log.e("CustomerListVM", "Failed to load customers: ${e.message}", e)
                    _customers.value = UiState.Error(e.message ?: "Failed to load customers")
                }
        }
    }

    /**
     * Create a customer from the list screen's FAB dialog, then refresh.
     */
    fun createCustomer(name: String, phone: String?, email: String?, address: String?) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            customerRepository.createCustomer(
                CreateCustomerRequest(
                    name = name.trim(),
                    phone = phone?.trim()?.ifBlank { null },
                    email = email?.trim()?.ifBlank { null },
                    addressLine1 = address?.trim()?.ifBlank { null },
                ),
            )
                .onSuccess {
                    _createState.value = UiState.Success(it)
                    loadCustomers(search = currentSearch)
                }
                .onFailure { e ->
                    Log.e("CustomerListVM", "Failed to create customer: ${e.message}", e)
                    _createState.value = UiState.Error(e.message ?: "Failed to create customer")
                }
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
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
                    hasMore = items.size >= 100
                }
                .onFailure { e ->
                    Log.e("CustomerListVM", "Failed to load more customers: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
