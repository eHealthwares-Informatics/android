package com.ehealthwares.rxsoft.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.CreateOrderItem
import com.ehealthwares.rxsoft.data.remote.dto.CreatePaymentLinkRequest
import com.ehealthwares.rxsoft.data.remote.dto.GenericProductDto
import com.ehealthwares.rxsoft.data.remote.dto.OrderDto
import com.ehealthwares.rxsoft.data.remote.dto.OrgNotificationChannelDto
import com.ehealthwares.rxsoft.data.local.CachedItemEntity
import com.ehealthwares.rxsoft.data.repository.OrderSubmitResult
import com.ehealthwares.rxsoft.data.repository.OrdersRepository
import com.ehealthwares.rxsoft.data.repository.PaymentsRepository
import com.ehealthwares.rxsoft.data.repository.PosRepository
import com.ehealthwares.rxsoft.ui.pos.PaymentLinkUiState
import com.ehealthwares.rxsoft.util.OfflineSyncManager
import com.ehealthwares.rxsoft.util.UiState
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
    private val paymentsRepository: PaymentsRepository,
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

    /** Org-configured notification channels (channel codes) for the send picker. */
    private val _orderChannels = MutableStateFlow<List<OrgNotificationChannelDto>>(emptyList())
    val orderChannels: StateFlow<List<OrgNotificationChannelDto>> = _orderChannels.asStateFlow()

    private val _orderPaymentLinkState = MutableStateFlow<PaymentLinkUiState>(PaymentLinkUiState.Idle)
    val orderPaymentLinkState: StateFlow<PaymentLinkUiState> = _orderPaymentLinkState.asStateFlow()

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

    /** Loads the org-configured notification channels for the send picker. */
    fun loadOrderChannels() {
        viewModelScope.launch {
            posRepository.getOrgNotificationChannels()
                .onSuccess { _orderChannels.value = it }
                .onFailure { Log.e("OrdersVM", "Failed to load org channels: ${it.message}", it) }
        }
    }

    /** Creates a payment link for an order, ready to be sent to the customer. */
    fun createOrderPaymentLink(order: OrderDto) {
        viewModelScope.launch {
            _orderPaymentLinkState.value = PaymentLinkUiState.Creating
            paymentsRepository.createPaymentLink(
                CreatePaymentLinkRequest(type = "order_payment", orderId = order.id)
            )
                .onSuccess {
                    _orderPaymentLinkState.value = PaymentLinkUiState.Ready(it.token, it.url)
                }
                .onFailure { e ->
                    Log.e("OrdersVM", "Failed to create order payment link: ${e.message}", e)
                    _orderPaymentLinkState.value =
                        PaymentLinkUiState.Error(e.message ?: "Failed to create payment link")
                }
        }
    }

    /** Sends the created order link over the chosen org channel. */
    fun sendOrderPaymentLink(channelId: String) {
        val state = _orderPaymentLinkState.value
        if (state !is PaymentLinkUiState.Ready) return
        viewModelScope.launch {
            _orderPaymentLinkState.value = PaymentLinkUiState.Sending
            paymentsRepository.sendPaymentLink(state.token, channelId)
                .onSuccess { _orderPaymentLinkState.value = PaymentLinkUiState.Sent(it.recipient) }
                .onFailure { e ->
                    Log.e("OrdersVM", "Failed to send order payment link: ${e.message}", e)
                    _orderPaymentLinkState.value =
                        PaymentLinkUiState.Error(e.message ?: "Failed to send payment link")
                }
        }
    }

    fun resetOrderPaymentLink() {
        _orderPaymentLinkState.value = PaymentLinkUiState.Idle
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
