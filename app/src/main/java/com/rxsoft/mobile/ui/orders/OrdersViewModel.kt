package com.rxsoft.mobile.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.CreateOrderItem
import com.rxsoft.mobile.data.remote.dto.GenericProductDto
import com.rxsoft.mobile.data.remote.dto.OrderDto
import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.repository.OrderSubmitResult
import com.rxsoft.mobile.data.repository.OrdersRepository
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.OfflineSyncManager
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/** A search hit in the New Order flow: an org item, or a generic product. */
sealed interface OrderSearchHit {
    data class Catalog(val item: CachedItemEntity) : OrderSearchHit
    data class Generic(val product: GenericProductDto) : OrderSearchHit
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val ordersRepository: OrdersRepository,
    private val posRepository: PosRepository,
    private val offlineSyncManager: OfflineSyncManager,
) : ViewModel() {

    private val _orders = MutableStateFlow<UiState<List<OrderDto>>>(UiState.Loading)
    val orders: StateFlow<UiState<List<OrderDto>>> = _orders.asStateFlow()

    private val _createState = MutableStateFlow<UiState<OrderSubmitResult>>(UiState.Idle)
    val createState: StateFlow<UiState<OrderSubmitResult>> = _createState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<OrderSearchHit>>(emptyList())
    val searchResults: StateFlow<List<OrderSearchHit>> = _searchResults.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    val pendingOrders = ordersRepository.observePendingOrders()
    val pendingCount = ordersRepository.observePendingCount()
    val isSyncing = offlineSyncManager.isSyncing

    init {
        loadOrders()
        // Keep the offline item cache warm (no-op when offline).
        viewModelScope.launch {
            ordersRepository.refreshItemCache()
                .onFailure { e -> Log.w("OrdersVM", "Item cache refresh skipped: ${e.message}") }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _orders.value = UiState.Loading
            ordersRepository.listOrders()
                .onSuccess { _orders.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("OrdersVM", "Failed to load orders: ${e.message}", e)
                    _orders.value = UiState.Error(e.message ?: "Failed to load orders")
                }
        }
    }

    /**
     * Search catalog items from the local SQLite cache (works offline) and
     * merge with generic-product results from the proxy (online only).
     */
    fun search(query: String) {
        val needle = query.trim()
        if (needle.length < 2) {
            _searchResults.value = emptyList()
            _searching.value = false
            return
        }
        viewModelScope.launch {
            _searching.value = true
            val cached = ordersRepository.searchItems(needle)
                .getOrElse { emptyList() }
                .map { OrderSearchHit.Catalog(it) }
            val generics = ordersRepository.searchGenericProducts(needle)
                .getOrElse { emptyList() }
                .map { OrderSearchHit.Generic(it) }
            _searchResults.value = cached + generics
            _searching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _searching.value = false
    }

    fun createOrder(
        paymentMethod: String,
        items: List<CreateOrderItem>,
    ) {
        if (items.isEmpty()) {
            _createState.value = UiState.Error("Add at least one item")
            return
        }
        viewModelScope.launch {
            _createState.value = UiState.Loading
            when (val result = ordersRepository.createOrder(paymentMethod, items)) {
                is OrderSubmitResult.Pushed -> _createState.value = UiState.Success(result)
                is OrderSubmitResult.Queued -> {
                    Log.i("OrdersVM", "Offline: order queued as ${result.clientRef}")
                    _createState.value = UiState.Success(result)
                }
            }
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    /** Manually trigger cache refresh + outbox push (also runs automatically on reconnect). */
    fun syncNow() {
        viewModelScope.launch { offlineSyncManager.refreshAndSync() }
    }

    companion object {
        fun buildItem(
            itemId: String? = null,
            freetextName: String? = null,
            genericItemCode: String? = null,
            quantity: Int,
            unitPrice: BigDecimal? = null,
        ): CreateOrderItem = CreateOrderItem(itemId, freetextName, genericItemCode, quantity, unitPrice)
    }
}
