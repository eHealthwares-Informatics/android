// Kotlin 1.9.24
// Kotlin/Composable // Last updated: 2026-10-06
package com.ehealthwares.rxsoft.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.remote.dto.OrderDto
import com.ehealthwares.rxsoft.data.remote.dto.OrderItemDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionItemsHeader
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionTotalRow
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.components.formatListTimestamp
import com.ehealthwares.rxsoft.ui.designsystem.components.prettyStatus
import com.ehealthwares.rxsoft.ui.designsystem.components.rememberNairaFormat
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.util.UiState
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import java.math.BigDecimal

private const val ALL_ORDERS = "All Orders"
private const val MAX_STATUS_CHIPS = 4

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

    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(ALL_ORDERS) }

    val allOrders = (ordersState as? UiState.Success)?.data.orEmpty()
    val statusOptions = remember(allOrders) {
        listOf(ALL_ORDERS) + allOrders.map { it.orderStatus }.distinct()
            .take(MAX_STATUS_CHIPS)
            .map { prettyStatus(it) }
    }

    val visibleOrders = remember(allOrders, selectedChip, searchText) {
        allOrders.filter { order ->
            val inStatus = selectedChip == ALL_ORDERS || prettyStatus(order.orderStatus) == selectedChip
            val n = searchText.trim().lowercase()
            val inSearch = n.isBlank() ||
                order.orderNumber.lowercase().contains(n) ||
                (order.paymentMethod ?: "").lowercase().contains(n) ||
                order.totalAmount.toPlainString().contains(n)
            inStatus && inSearch
        }
    }

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
            title = "Orders",
            state = ordersState,
            onBack = onBack,
            onMenuClick = onMenuClick,
            subtitle = "Customer Orders",
            headerIcon = Icons.Outlined.ShoppingCart,
            onSearchClick = { searchOpen = !searchOpen },
            onAddClick = onNewOrder,
            addDescription = "New order",
            filters = {
                Column {
                androidx.compose.animation.AnimatedVisibility(visible = searchOpen) {
                    AppSearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it },
                        placeholder = "Search order no., method, amount",
                        modifier = Modifier.padding(bottom = SpacingTokens.xs),
                    )
                }
                FilterChipRow(
                    options = statusOptions,
                    selected = selectedChip,
                    onSelect = { selectedChip = it },
                )
                }
            },
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
            listContent = { _ ->
                if (visibleOrders.isEmpty() && allOrders.isNotEmpty()) {
                    item {
                        Text(
                            "No orders match this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(SpacingTokens.lg),
                        )
                    }
                }
                items(visibleOrders, key = { it.id }) { order ->
                    OrderAccordionCard(
                        order = order,
                        expanded = expandedId == order.id,
                        onToggle = { expandedId = if (expandedId == order.id) null else order.id },
                    )
                }
            },
        )
    }
}

@Composable
private fun OrderAccordionCard(
    order: OrderDto,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    AccordionListCard(
        icon = Icons.Outlined.ShoppingBag,
        title = order.orderNumber,
        dateLabel = formatListTimestamp(order.createdAt),
        metaLabel = "${order.items?.size ?: 0} item(s) · ${order.paymentMethod ?: "—"}",
        statusText = order.orderStatus,
        amount = order.totalAmount,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        AccordionItemsHeader(label = "Items (${order.items?.size ?: 0})")
        val items = order.items.orEmpty()
        if (items.isEmpty()) {
            Text(
                "No lines on this order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
            )
        } else {
            items.forEach { line -> OrderItemRow(line = line) }
        }
        Spacer(Modifier.height(SpacingTokens.sm))
        AccordionTotalRow(
            label = "Total Amount",
            amount = order.totalAmount,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.height(SpacingTokens.xs))
    }
}

@Composable
private fun OrderItemRow(line: OrderItemDto) {
    val format = rememberNairaFormat()
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    // A line may combine kinds (e.g. freetext + generic code).
    val labels = buildList {
        line.freetextName?.let { add(it) }
        line.genericItemCode?.let { add("$it (generic)") }
        if (isEmpty()) line.item?.name?.let { add(it) }
        if (isEmpty()) line.itemId?.let { add("Item #${it.take(8)}") }
    }
    val kindLabel = buildList {
        if (line.itemId != null) add("item")
        if (line.freetextName != null) add("freetext")
        if (line.genericItemCode != null) add("generic")
    }.joinToString(" + ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "$kindLabel · × ${stripQty(line.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = mutedColor,
            )
        }
        Text(
            format.format(line.unitPrice * line.quantity),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
    }
}

private fun stripQty(q: BigDecimal): String = q.stripTrailingZeros().toPlainString()
