package com.rxsoft.mobile.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.OrderDto
import com.rxsoft.mobile.data.remote.dto.OrderItemDto
import com.rxsoft.mobile.data.repository.OrdersRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One order line flattened from an order, prefixed with its parent order context. */
data class OrderLineRow(
    val orderId: String,
    val lineId: String,
    val orderNumber: String,
    val orderStatus: String,
    val createdAt: String?,
    val itemName: String,
    val kind: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal,
)

/** Order Lines screen: every line item across website/ERP orders. */
@HiltViewModel
class OrderLinesViewModel @Inject constructor(
    private val ordersRepository: OrdersRepository,
) : ViewModel() {

    private val _lines = MutableStateFlow<UiState<List<OrderLineRow>>>(UiState.Loading)
    val lines: StateFlow<UiState<List<OrderLineRow>>> = _lines.asStateFlow()

    init {
        loadLines()
    }

    fun loadLines() {
        viewModelScope.launch {
            _lines.value = UiState.Loading
            ordersRepository.listOrders()
                .map { orders -> flattenOrderLines(orders) }
                .onSuccess { _lines.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("OrderLinesVM", "Failed to load order lines: ${e.message}", e)
                    _lines.value = UiState.Error(e.message ?: "Failed to load order lines")
                }
        }
    }

    private fun flattenOrderLines(orders: List<OrderDto>): List<OrderLineRow> {
        return orders.flatMap { order ->
            order.items.orEmpty().map { item ->
                OrderLineRow(
                    orderId = order.id,
                    lineId = item.id,
                    orderNumber = order.orderNumber,
                    orderStatus = order.orderStatus,
                    createdAt = order.createdAt,
                    itemName = orderLineLabel(item),
                    kind = orderLineKind(item),
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    lineTotal = item.quantity * item.unitPrice,
                )
            }
        }
    }
}

private fun orderLineLabel(item: OrderItemDto): String {
    item.freetextName?.let { return it }
    item.genericItemCode?.let { return it }
    item.item?.name?.let { return it }
    item.item?.code?.let { return it }
    return item.itemId ?: item.id
}

private fun orderLineKind(item: OrderItemDto): String {
    if (item.freetextName != null) return "Freetext"
    if (item.genericItemCode != null) return "Generic"
    return "Item"
}