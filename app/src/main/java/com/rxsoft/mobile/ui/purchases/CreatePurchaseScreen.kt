package com.rxsoft.mobile.ui.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.rxsoft.mobile.data.remote.dto.PartyDto
import com.rxsoft.mobile.data.remote.dto.StockLocationDto
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppSecondaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.math.BigDecimal

/**
 * Full-screen "New Purchase" flow: supplier + warehouse pickers, item search,
 * editable line list, and optional invoice number / note. On success it pops
 * back to the purchase list.
 */
@Composable
fun CreatePurchaseScreen(
    viewModel: PurchasesViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val createState by viewModel.createState.collectAsState()
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

    // Reset stale state on entry; clean up on exit.
    DisposableEffect(Unit) {
        viewModel.resetCreateState()
        onDispose { viewModel.resetCreateState() }
    }

    // Navigate back once the purchase has been created.
    val successData = createState as? UiState.Success
    androidx.compose.runtime.LaunchedEffect(successData) {
        if (successData != null) onSaved()
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "New Purchase",
                onBack = onBack,
            )
        },
        bottomBar = {
            Surface {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.sm),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    AppSecondaryButton(
                        text = "Cancel",
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                    )
                    AppPrimaryButton(
                        text = if (createState is UiState.Loading) "Creating…" else "Create PO",
                        enabled = selectedSupplier != null &&
                            selectedWarehouse != null &&
                            lines.isNotEmpty() &&
                            lines.all { it.uomId.isNotEmpty() } &&
                            createState !is UiState.Loading,
                        onClick = {
                            viewModel.createPurchase(
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
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.sm),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // ── Supplier picker ─────────────────────────────────────────────
            Text("Supplier", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = supplierQuery,
                onValueChange = { supplierQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search supplier") },
                singleLine = true,
            )
            if (selectedSupplier != null) {
                Text(
                    "Selected: ${selectedSupplier!!.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                suppliers.filter {
                    supplierQuery.isBlank() || it.name.contains(supplierQuery, ignoreCase = true)
                }.take(5).forEach { supplier ->
                    TextButton(onClick = {
                        selectedSupplier = supplier
                        supplierQuery = supplier.name
                    }) { Text(supplier.name) }
                }
                if (suppliers.isEmpty()) {
                    Text(
                        "No suppliers loaded. Pull to refresh the list screen while online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            // ── Warehouse picker ────────────────────────────────────────────
            Text("Warehouse", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = warehouseQuery,
                onValueChange = { warehouseQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search warehouse") },
                singleLine = true,
            )
            if (selectedWarehouse != null) {
                Text(
                    "Selected: ${selectedWarehouse!!.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                warehouses.filter {
                    warehouseQuery.isBlank() || it.name.contains(warehouseQuery, ignoreCase = true)
                }.take(5).forEach { warehouse ->
                    TextButton(onClick = {
                        selectedWarehouse = warehouse
                        warehouseQuery = warehouse.name
                    }) { Text(warehouse.name) }
                }
                if (warehouses.isEmpty()) {
                    Text(
                        "No warehouses loaded. Pull to refresh the list screen while online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            // ── Line builder ────────────────────────────────────────────────
            Text("Add item", style = MaterialTheme.typography.titleSmall)
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
                items.take(5).forEach { item ->
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

            HorizontalDivider()

            // ── Selected lines ──────────────────────────────────────────────
            Text("Lines (${lines.size})", style = MaterialTheme.typography.titleSmall)
            if (lines.isEmpty()) {
                Text(
                    "Search an item above and tap Add.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                lines.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(line.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${line.quantity} × ${line.unitCost}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { lines = lines - line }) { Text("Remove") }
                    }
                }
            }

            HorizontalDivider()

            // ── Optional fields ─────────────────────────────────────────────
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

            val errorState = createState as? UiState.Error
            if (errorState != null) {
                Text(
                    errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(SpacingTokens.lg))
        }
    }
}
