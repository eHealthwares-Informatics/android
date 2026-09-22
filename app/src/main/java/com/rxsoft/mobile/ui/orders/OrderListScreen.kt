// Kotlin 1.9.24
// Kotlin/Composable // Last updated: 2026-09-09 updated by med
package com.rxsoft.mobile.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.OrderDto
import com.rxsoft.mobile.data.remote.dto.OrderItemDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderListScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onNewOrder: () -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val ordersState by viewModel.orders.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState(initial = 0)
    val isSyncing by viewModel.isSyncing.collectAsState()

    var detailOrder by remember { mutableStateOf<OrderDto?>(null) }

    Column {
        if (pendingCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "No connection — order(s) saved on device and will sync automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
                )
            }
        }
        ListScreenTemplate(
            title = "Order",
            state = ordersState,
            onBack = onBack,
            onMenuClick = onMenuClick,
            onRefresh = {
                viewModel.loadOrders()
                viewModel.syncNow()
            },
            emptyTitle = "No orders yet",
            emptySubtitle = "Tap + to create your first order",
            topBarActions = {
                if (pendingCount > 0) {
                    TextButton(
                        onClick = { viewModel.syncNow() },
                        enabled = !isSyncing,
                    ) {
                        Text(if (isSyncing) "Syncing…" else "Sync ($pendingCount)")
                    }
                }
            },
            fab = {
                FloatingActionButton(onClick = onNewOrder) {
                    Icon(Icons.Filled.Add, contentDescription = "New order")
                }
            },
            listContent = { orders ->
                items(orders, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { detailOrder = order })
                }
            },
        )
    }

    detailOrder?.let { order ->
        OrderDetailDialog(order = order, onDismiss = { detailOrder = null })
    }
}

@Composable
internal fun OrderCard(order: OrderDto, onClick: () -> Unit) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AppCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(order.orderNumber, fontWeight = FontWeight.Bold)
            Text(order.orderStatus.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(SpacingTokens.xs))
        Text(
            "${order.items?.size ?: 0} item(s) · ${order.paymentMethod ?: "-"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            format.format(order.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
internal fun OrderDetailDialog(order: OrderDto, onDismiss: () -> Unit) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(order.orderNumber) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                Text("Status: ${order.orderStatus.uppercase()}", style = MaterialTheme.typography.bodySmall)
                order.items?.forEach { item ->
                    OrderLineRow(item = item, format = format)
                }
                Text(
                    "Total: ${format.format(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
internal fun OrderLineRow(item: OrderItemDto, format: NumberFormat) {
    // A line may combine kinds (e.g. freetext + generic code).
    val labels = buildList {
        item.freetextName?.let { add(it) }
        item.genericItemCode?.let { add("$it (generic)") }
        if (isEmpty()) item.item?.name?.let { add(it) }
        if (isEmpty()) item.itemId?.let { add("Item #${it.take(8)}") }
    }
    val kindLabel = buildList {
        if (item.itemId != null) add("item")
        if (item.freetextName != null) add("freetext")
        if (item.genericItemCode != null) add("generic")
    }.joinToString(" + ")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            labels.forEach { label ->
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "$kindLabel · qty ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(format.format(item.unitPrice * item.quantity), style = MaterialTheme.typography.bodyMedium)
    }
}

