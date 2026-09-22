package com.rxsoft.mobile.ui.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.PartyDto
import com.rxsoft.mobile.data.remote.dto.PurchaseDto
import com.rxsoft.mobile.data.remote.dto.StockLocationDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppSecondaryButton
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PurchaseListScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    viewModel: PurchasesViewModel = hiltViewModel(),
) {
    val purchasesState by viewModel.purchases.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var detailPurchase by remember { mutableStateOf<String?>(null) }

    ListScreenTemplate(
        title = "Purchase",
        state = purchasesState,
        onBack = onBack,
        onMenuClick = onMenuClick,
        onRefresh = { viewModel.loadPurchases() },
        emptyTitle = "No purchase orders yet",
        emptySubtitle = "Tap + to create your first purchase order",
        fab = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Purchase")
            }
        },
    ) { data ->
        items(data, key = { it.id }) { purchase ->
            PurchaseCard(purchase = purchase, onClick = { detailPurchase = purchase.id })
        }
    }

    if (showCreate) {
        CreatePurchaseDialog(
            createState = createState,
            viewModel = viewModel,
            onSubmit = { supplierId, warehouseId, lines, note, invoiceNumber ->
                viewModel.createPurchase(supplierId, warehouseId, lines, note, invoiceNumber)
            },
            onDismiss = {
                showCreate = false
                viewModel.resetCreateState()
            },
        )
    }

    detailPurchase?.let { id ->
        PurchaseDetailDialog(
            purchaseId = id,
            viewModel = viewModel,
            actionState = actionState,
            onClose = {
                detailPurchase = null
                viewModel.clearDetail()
                viewModel.resetActionState()
            },
        )
    }
}

@Composable
private fun PurchaseCard(purchase: PurchaseDto, onClick: () -> Unit) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AppCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(purchase.invoiceNumber, fontWeight = FontWeight.Bold)
            Text(purchase.status.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(SpacingTokens.xs))
        Text(
            "${purchase.supplier?.name ?: "Unknown supplier"} · ${purchase.lines?.size ?: 0} line(s)",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            format.format(purchase.totalCost),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CreatePurchaseDialog(
    createState: UiState<PurchaseDto>,
    viewModel: PurchasesViewModel,
    onSubmit: (supplierId: String, warehouseId: String, lines: List<com.rxsoft.mobile.data.remote.dto.CreatePurchaseLine>, note: String?, invoiceNumber: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val uoms by viewModel.uoms.collectAsState()
    val items by viewModel.items.collectAsState()
    val searchingItems by viewModel.searchingItems.collectAsState()

    var supplierQuery by remember { mutableStateOf("") }
    var selectedSupplier by remember { mutableStateOf<PartyDto?>(null) }
    var warehouseQuery by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf<StockLocationDto?>(null) }
    var itemQuery by remember { mutableStateOf("") }
    var lines by remember { mutableStateOf(listOf<DraftPurchaseLine>()) }
    var quantityText by remember { mutableStateOf("1") }
    var costText by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Purchase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                // Supplier picker (search over loaded suppliers)
                OutlinedTextField(
                    value = supplierQuery,
                    onValueChange = { supplierQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Supplier") },
                    singleLine = true,
                )
                if (selectedSupplier != null) {
                    Text(
                        "Supplier: ${selectedSupplier!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    suppliers.filter {
                        supplierQuery.isBlank() || it.name.contains(supplierQuery, ignoreCase = true)
                    }.take(4).forEach { supplier ->
                        TextButton(onClick = {
                            selectedSupplier = supplier
                            supplierQuery = supplier.name
                        }) { Text(supplier.name) }
                    }
                }

                // Warehouse picker (search over loaded stock locations)
                OutlinedTextField(
                    value = warehouseQuery,
                    onValueChange = { warehouseQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Warehouse") },
                    singleLine = true,
                )
                if (selectedWarehouse != null) {
                    Text(
                        "Warehouse: ${selectedWarehouse!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    warehouses.filter {
                        warehouseQuery.isBlank() || it.name.contains(warehouseQuery, ignoreCase = true)
                    }.take(4).forEach { warehouse ->
                        TextButton(onClick = {
                            selectedWarehouse = warehouse
                            warehouseQuery = warehouse.name
                        }) { Text(warehouse.name) }
                    }
                }

                // Item search
                OutlinedTextField(
                    value = itemQuery,
                    onValueChange = { query ->
                        itemQuery = query
                        viewModel.searchItems(query)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search items") },
                    singleLine = true,
                )
                if (searchingItems) {
                    Text("Searching…", style = MaterialTheme.typography.bodySmall)
                } else {
                    items.take(4).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    item.code ?: "item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(
                                onClick = {
                                    lines = lines + DraftPurchaseLine(
                                        itemId = item.id,
                                        label = item.name,
                                        uomId = item.baseUomId ?: "",
                                        quantity = quantityText.toIntOrNull() ?: 1,
                                        unitCost = costText.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    )
                                },
                                enabled = (item.baseUomId ?: "").isNotEmpty(),
                            ) { Text("Add") }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Unit cost") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                }

                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Invoice/PO number (optional)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (optional)") },
                    singleLine = true,
                )

                if (lines.isNotEmpty()) {
                    Text("Lines (${lines.size})", style = MaterialTheme.typography.titleSmall)
                    lines.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${line.quantity} × ${line.label}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { lines = lines - line }) { Text("Remove") }
                        }
                    }
                }

                if (createState is UiState.Error) {
                    Text(
                        createState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = if (createState is UiState.Loading) "Creating…" else "Create PO",
                enabled = selectedSupplier != null &&
                    selectedWarehouse != null &&
                    lines.isNotEmpty() &&
                    lines.all { it.uomId.isNotEmpty() } &&
                    createState !is UiState.Loading,
                onClick = {
                    onSubmit(
                        selectedSupplier!!.id,
                        selectedWarehouse!!.id,
                        lines.map { line ->
                            PurchasesViewModel.buildLine(
                                itemId = line.itemId,
                                orderedQty = BigDecimal(line.quantity),
                                uomId = line.uomId,
                                unitCost = line.unitCost,
                            )
                        },
                        note.ifBlank { null },
                        invoiceNumber.ifBlank { null },
                    )
                },
            )
        },
        dismissButton = {
            AppSecondaryButton(text = "Cancel", onClick = onDismiss)
        },
    )
}

internal data class DraftPurchaseLine(
    val itemId: String,
    val label: String,
    val uomId: String,
    val quantity: Int,
    val unitCost: BigDecimal,
)

@Composable
private fun PurchaseDetailDialog(
    purchaseId: String,
    viewModel: PurchasesViewModel,
    actionState: UiState<PurchaseDto>,
    onClose: () -> Unit,
) {
    LaunchedEffect(purchaseId) { viewModel.loadDetail(purchaseId) }

    val detail by viewModel.detail.collectAsState()

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            when (detail) {
                is UiState.Success -> Text((detail as UiState.Success<PurchaseDto>).data.invoiceNumber)
                else -> Text("Purchase Order")
            }
        },
        text = {
            when (detail) {
                is UiState.Loading -> Text("Loading…")
                is UiState.Error -> Text((detail as UiState.Error).message)
                is UiState.Success -> {
                    val purchase = (detail as UiState.Success<PurchaseDto>).data
                    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
                    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                        Text(
                            "${purchase.supplier?.name ?: "-"} · ${purchase.warehouse?.name ?: "-"}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text("Status: ${purchase.status.uppercase()}", style = MaterialTheme.typography.bodySmall)
                        purchase.lines?.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(line.itemName ?: line.itemId ?: "Item", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "ordered ${line.orderedQty} · received ${line.receivedQty}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(format.format(line.lineTotal), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Text(
                            "Total: ${format.format(purchase.totalCost)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (actionState is UiState.Error) {
                            Text(
                                actionState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (actionState is UiState.Loading) {
                            Text("Working…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> Text("—")
            }
        },
        confirmButton = {
            val purchase = (detail as? UiState.Success<PurchaseDto>)?.data
            when {
                purchase?.status == "draft" -> AppPrimaryButton(
                    text = "Approve",
                    enabled = actionState !is UiState.Loading,
                    onClick = { viewModel.approvePurchase(purchase.id) },
                )
                purchase?.status == "approved" || purchase?.status == "partially_received" -> AppPrimaryButton(
                    text = "Receive All",
                    enabled = actionState !is UiState.Loading,
                    onClick = {
                        viewModel.receiveGoods(
                            purchase.id,
                            purchase.lines
                                ?.filter { it.receivedQty < it.orderedQty }
                                ?.map { line ->
                                    PurchasesViewModel.buildReceiveLine(
                                        itemId = line.itemId ?: line.itemCode ?: "",
                                        receivedQty = line.orderedQty - line.receivedQty,
                                        unitCost = line.unitCost,
                                        uomId = line.uomId ?: "",
                                    )
                                }
                                .orEmpty(),
                        )
                    },
                )
                else -> AppSecondaryButton(text = "Close", onClick = onClose)
            }
        },
        dismissButton = {
            val current = (detail as? UiState.Success<PurchaseDto>)?.data
            if (current?.status == "draft" || current?.status == "approved" || current?.status == "partially_received") {
                AppSecondaryButton(text = "Close", onClick = onClose)
            }
        },
    )
}
