package com.ehealthwares.rxsoft.ui.purchases

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.remote.dto.PurchaseDto
import com.ehealthwares.rxsoft.data.remote.dto.PurchaseLineDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionItemsHeader
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionTotalRow
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.components.formatListTimestamp
import com.ehealthwares.rxsoft.ui.designsystem.components.prettyStatus
import com.ehealthwares.rxsoft.ui.designsystem.components.rememberNairaFormat
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.ui.designsystem.components.isListThemeDark
import com.ehealthwares.rxsoft.data.remote.dto.ReceiveGoodsLine
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState

private const val ALL_PURCHASES = "All"
private const val MAX_STATUS_CHIPS = 4

@Composable
fun PurchaseListScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onCreatePurchase: () -> Unit = {},
    viewModel: PurchasesViewModel = hiltViewModel(),
) {
    val purchasesState by viewModel.purchases.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val details by viewModel.details.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(ALL_PURCHASES) }

    val allPurchases = (purchasesState as? UiState.Success)?.data.orEmpty()
    val statusOptions = remember(allPurchases) {
        listOf(ALL_PURCHASES) + allPurchases.map { it.status }.distinct()
            .take(MAX_STATUS_CHIPS)
            .map { prettyStatus(it) }
    }

    val visiblePurchases = remember(allPurchases, selectedChip, searchText) {
        allPurchases.filter { purchase ->
            val inStatus = selectedChip == ALL_PURCHASES || prettyStatus(purchase.status) == selectedChip
            val n = searchText.trim().lowercase()
            val inSearch = n.isBlank() ||
                purchase.invoiceNumber.lowercase().contains(n) ||
                (purchase.supplier?.name ?: "").lowercase().contains(n) ||
                purchase.totalCost.toPlainString().contains(n)
            inStatus && inSearch
        }
    }

    ListScreenTemplate(
        title = "Purchases",
        state = purchasesState,
        onBack = onBack,
        onMenuClick = onMenuClick,
        subtitle = "Purchase Orders",
        headerIcon = Icons.Outlined.LocalShipping,
        onSearchClick = { searchOpen = !searchOpen },
        onAddClick = onCreatePurchase,
        addDescription = "New Purchase",
        filters = {
            Column {
            AnimatedVisibility(visible = searchOpen) {
                AppSearchBar(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    placeholder = "Search PO no., supplier, amount",
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
        isLoadingMore = isLoadingMore,
        onRefresh = { viewModel.loadPurchases() },
        onLoadMore = {
            // Infinite scroll: don't cascade page loads while a narrow
            // status/search filter hides rows.
            val filtering = selectedChip != ALL_PURCHASES || searchText.isNotBlank()
            if (!filtering || visiblePurchases.size >= 10) viewModel.loadMorePurchases()
        },
        emptyTitle = "No purchase orders yet",
        emptySubtitle = "Tap + to create your first purchase order",
    ) { _ ->
        if (visiblePurchases.isEmpty() && allPurchases.isNotEmpty()) {
            item {
                Text(
                    "No purchase orders match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(SpacingTokens.lg),
                )
            }
        }
        items(visiblePurchases, key = { it.id }) { purchase ->
            PurchaseAccordionCard(
                purchase = purchase,
                detailState = details[purchase.id],
                actionState = actionState,
                expanded = expandedId == purchase.id,
                onToggle = { expandedId = if (expandedId == purchase.id) null else purchase.id },
                onLoadDetail = { viewModel.loadDetailFor(purchase.id) },
                onApprove = {
                    viewModel.invalidateDetail(purchase.id)
                    viewModel.approvePurchase(purchase.id)
                },
                onReceiveAll = { lines ->
                    viewModel.invalidateDetail(purchase.id)
                    viewModel.receiveGoods(purchase.id, lines)
                },
            )
        }
    }
}

@Composable
private fun PurchaseAccordionCard(
    purchase: PurchaseDto,
    detailState: UiState<PurchaseDto>?,
    actionState: UiState<PurchaseDto>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onLoadDetail: () -> Unit,
    onApprove: () -> Unit,
    onReceiveAll: (List<com.ehealthwares.rxsoft.data.remote.dto.ReceiveGoodsLine>) -> Unit,
) {
    // Lines may ride on the list response; otherwise fetch on expand (lazy).
    LaunchedEffect(expanded, purchase.id) {
        if (expanded && purchase.lines == null && detailState == null) {
            onLoadDetail()
        }
    }

    val lines: List<PurchaseLineDto>? = purchase.lines
        ?: (detailState as? UiState.Success)?.data?.lines
    val current = (detailState as? UiState.Success)?.data ?: purchase
    var showReceiveDialog by remember { mutableStateOf(false) }

    AccordionListCard(
        icon = Icons.Outlined.LocalShipping,
        title = purchase.invoiceNumber,
        dateLabel = formatListTimestamp(purchase.orderDate),
        metaLabel = listOf(
            purchase.supplier?.name ?: "Unknown supplier",
            purchase.warehouse?.name,
        ).filterNotNull().joinToString(" · "),
        statusText = purchase.status,
        amount = purchase.totalCost,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        AccordionItemsHeader(label = "Lines (${lines?.size ?: 0})")
        when {
            lines != null -> {
                if (lines.isEmpty()) {
                    Text(
                        "No lines on this purchase order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                    )
                } else {
                    lines.forEach { line -> PurchaseLineRow(line = line) }
                }
                Spacer(Modifier.height(SpacingTokens.sm))
                AccordionTotalRow(
                    label = "Total Amount",
                    amount = purchase.totalCost,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                if (actionState is UiState.Error) {
                    Text(
                        actionState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (actionState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    when (current.status) {
                        "draft" -> AppPrimaryButton(
                            text = "Approve",
                            enabled = actionState !is UiState.Loading,
                            onClick = onApprove,
                        )
                        "approved", "partially_received" -> AppPrimaryButton(
                            text = "Receive",
                            enabled = actionState !is UiState.Loading &&
                                lines.any { it.receivedQty < it.orderedQty },
                            onClick = { showReceiveDialog = true },
                        )
                    }
                }
            }
            detailState is UiState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading lines…", style = MaterialTheme.typography.bodySmall)
                }
            }
            detailState is UiState.Error -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        detailState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = onLoadDetail) { Text("Retry") }
                }
            }
            else -> {}
        }
    }

    if (showReceiveDialog && lines != null) {
        ReceiveDialog(
            lines = lines.filter { it.receivedQty < it.orderedQty },
            submitting = actionState is UiState.Loading,
            error = (actionState as? UiState.Error)?.message,
            onConfirm = { payload ->
                showReceiveDialog = false
                onReceiveAll(payload)
            },
            onDismiss = { showReceiveDialog = false },
        )
    }
}

/**
 * Receive-goods dialog with a per-line received-quantity input (same pattern
 * as the stock-adjustment dialog on the New Sale screen). Blank/zero skips a
 * line; a single request posts every entered line.
 */
@Composable
private fun ReceiveDialog(
    lines: List<PurchaseLineDto>,
    submitting: Boolean,
    error: String?,
    onConfirm: (List<ReceiveGoodsLine>) -> Unit,
    onDismiss: () -> Unit,
) {
    val qtyByLine = remember(lines) {
        mutableStateMapOf<String, String>().apply {
            lines.forEach { put(it.id, stripQty(it.orderedQty - it.receivedQty)) }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Receive goods") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                Text(
                    "Enter the quantity received for each line. Leave 0 to skip a line.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                lines.forEach { line ->
                    val outstanding = line.orderedQty - line.receivedQty
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            line.itemName ?: line.itemCode ?: "Item",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "Ordered ${stripQty(line.orderedQty)} · received ${stripQty(line.receivedQty)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val entered = qtyByLine[line.id]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        OutlinedTextField(
                            value = qtyByLine[line.id] ?: "",
                            onValueChange = { v -> qtyByLine[line.id] = v },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Received quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !submitting,
                            supportingText = {
                                if (entered > outstanding) {
                                    Text(
                                        "Exceeds outstanding ${stripQty(outstanding)}",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            },
                        )
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Receive",
                enabled = !submitting && lines.any {
                    (qtyByLine[it.id]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO) > java.math.BigDecimal.ZERO
                },
                onClick = {
                    val payload = lines.mapNotNull { line ->
                        val qty = qtyByLine[line.id]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        if (qty <= java.math.BigDecimal.ZERO) {
                            null
                        } else {
                            PurchasesViewModel.buildReceiveLine(
                                itemId = line.itemId ?: line.itemCode ?: "",
                                receivedQty = qty,
                                unitCost = line.unitCost,
                                uomId = line.uomId ?: "",
                            )
                        }
                    }
                    if (payload.isNotEmpty()) onConfirm(payload)
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancel") }
        },
    )
}

@Composable
private fun PurchaseLineRow(line: PurchaseLineDto) {
    val format = rememberNairaFormat()
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.itemName ?: line.itemCode ?: "Item",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "ordered ${stripQty(line.orderedQty)} · received ${stripQty(line.receivedQty)}" +
                    (line.uomName?.let { " $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = mutedColor,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = format.format(line.unitCost),
                style = MaterialTheme.typography.bodySmall,
                color = mutedColor,
            )
            Text(
                text = format.format(line.lineTotal),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
        }
    }
}

private fun stripQty(q: java.math.BigDecimal): String = q.stripTrailingZeros().toPlainString()
