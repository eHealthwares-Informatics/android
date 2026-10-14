package com.ehealthwares.rxsoft.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.data.remote.dto.PartyDto
import com.ehealthwares.rxsoft.data.remote.dto.StockLocationDto
import com.ehealthwares.rxsoft.data.remote.dto.UomDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSecondaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.DamorexColors
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState
import java.math.BigDecimal

/**
 * Full-screen "New Purchase" flow modelled on the web purchases page:
 * supplier + warehouse pickers, item search, per-line qty / unit cost / UOM
 * editing, then **Save Draft** or **Submit & Approve**.
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

    val linesValid = lines.isNotEmpty() &&
        lines.all { it.quantity > BigDecimal.ZERO && it.uomId.isNotEmpty() }
    val canSubmit = selectedSupplier != null &&
        selectedWarehouse != null &&
        linesValid &&
        createState !is UiState.Loading

    fun submit(status: String) {
        val supplier = selectedSupplier ?: return
        val warehouse = selectedWarehouse ?: return
        viewModel.createPurchase(
            supplierId = supplier.id,
            warehouseId = warehouse.id,
            lines = lines.map { line ->
                PurchasesViewModel.buildLine(
                    itemId = line.itemId,
                    orderedQty = line.quantity,
                    uomId = line.uomId,
                    unitCost = line.unitCost,
                )
            },
            note = note.ifBlank { null },
            invoiceNumber = invoiceNumber.ifBlank { null },
            status = status,
        )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.sm),
                ) {
                    if (createState is UiState.Error) {
                        Text(
                            (createState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = SpacingTokens.xs),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                        AppSecondaryButton(
                            text = if (createState is UiState.Loading) "Saving…" else "Save Draft",
                            enabled = canSubmit,
                            onClick = { submit("draft") },
                            modifier = Modifier.weight(1f),
                        )
                        AppPrimaryButton(
                            text = if (createState is UiState.Loading) "Submitting…" else "Submit & Approve",
                            enabled = canSubmit,
                            onClick = { submit("approved") },
                            modifier = Modifier.weight(1f),
                        )
                    }
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
            SectionLabel("Supplier")
            OutlinedTextField(
                value = supplierQuery,
                onValueChange = { supplierQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search supplier") },
                singleLine = true,
            )
            if (selectedSupplier != null) {
                SelectedChip("Supplier: ${selectedSupplier!!.name}") {
                    selectedSupplier = null
                    supplierQuery = ""
                }
            } else {
                suppliers.filter {
                    supplierQuery.isBlank() || it.name.contains(supplierQuery, ignoreCase = true)
                }.take(5).forEach { supplier ->
                    PickerRow(supplier.name, supplier.phone) {
                        selectedSupplier = supplier
                        supplierQuery = supplier.name
                    }
                }
                if (suppliers.isEmpty()) {
                    Text(
                        "No suppliers loaded. Pull to refresh the list screen while online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = DamorexColors.border)

            // ── Warehouse picker ────────────────────────────────────────────
            SectionLabel("Warehouse")
            OutlinedTextField(
                value = warehouseQuery,
                onValueChange = { warehouseQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search warehouse") },
                singleLine = true,
            )
            if (selectedWarehouse != null) {
                SelectedChip("Warehouse: ${selectedWarehouse!!.name}") {
                    selectedWarehouse = null
                    warehouseQuery = ""
                }
            } else {
                warehouses.filter {
                    warehouseQuery.isBlank() || it.name.contains(warehouseQuery, ignoreCase = true)
                }.take(5).forEach { warehouse ->
                    PickerRow(warehouse.name, warehouse.code) {
                        selectedWarehouse = warehouse
                        warehouseQuery = warehouse.name
                    }
                }
                if (warehouses.isEmpty()) {
                    Text(
                        "No warehouses loaded. Pull to refresh the list screen while online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = DamorexColors.border)

            // ── Add item via search ─────────────────────────────────────────
            SectionLabel("Add item")
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
                    PickerRow(item.name, item.code) {
                        lines = lines + DraftPurchaseLine(
                            itemId = item.id,
                            label = item.name,
                            itemCode = item.code,
                            uomId = item.baseUomId ?: "",
                        )
                    }
                }
                if (itemQuery.length >= 2 && items.isEmpty()) {
                    Text(
                        "No items matched.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = DamorexColors.border)

            // ── Lines editor ────────────────────────────────────────────────
            SectionLabel("Lines (${lines.size})")
            if (lines.isEmpty()) {
                Text(
                    "Search an item above to add a line.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                lines.forEachIndexed { index, line ->
                    LineEditorRow(
                        line = line,
                        uoms = uoms,
                        onRemove = { lines = lines - line },
                        onUpdate = { updated ->
                            lines = lines.toMutableList().also { it[index] = updated }
                        },
                    )
                }
                val total = lines.fold(BigDecimal.ZERO) { acc, l -> acc + l.lineTotal }
                Text(
                    "Total: ₦${total.toPlainString()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DamorexColors.greenDark,
                    modifier = Modifier.align(Alignment.End),
                )
            }

            HorizontalDivider(color = DamorexColors.border)

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

            Spacer(Modifier.height(SpacingTokens.lg))
        }
    }
}

/** One editable purchase line: label, qty, unit cost, UOM picker, remove. */
@Composable
private fun LineEditorRow(
    line: DraftPurchaseLine,
    uoms: List<UomDto>,
    onRemove: () -> Unit,
    onUpdate: (DraftPurchaseLine) -> Unit,
) {
    var uomMenuOpen by remember { mutableStateOf(false) }
    val selectedUom = uoms.firstOrNull { it.id == line.uomId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    line.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                line.itemCode?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove line",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = line.quantityText,
                onValueChange = { v -> onUpdate(line.copy(quantityText = v.filter { c -> c.isDigit() || c == '.' })) },
                modifier = Modifier.weight(1f),
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            OutlinedTextField(
                value = line.costText,
                onValueChange = { v -> onUpdate(line.copy(costText = v.filter { c -> c.isDigit() || c == '.' })) },
                modifier = Modifier.weight(1.4f),
                label = { Text("Unit cost") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            Box(modifier = Modifier.weight(1.2f)) {
                OutlinedTextField(
                    value = selectedUom?.name ?: if (line.uomId.isBlank()) "" else "UOM",
                    onValueChange = {},
                    readOnly = true,
                    enabled = line.uomId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("UOM") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick UOM")
                    },
                    singleLine = true,
                )
                // Overlay captures taps so the read-only field opens the picker.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = line.uomId.isNotBlank()) { uomMenuOpen = true },
                )
                DropdownMenu(
                    expanded = uomMenuOpen,
                    onDismissRequest = { uomMenuOpen = false },
                    modifier = Modifier.heightIn(max = 320.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        uoms.forEach { uom ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(uom.name, style = MaterialTheme.typography.bodyMedium)
                                        uom.code?.let {
                                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                onClick = {
                                    onUpdate(line.copy(uomId = uom.id))
                                    uomMenuOpen = false
                                },
                            )
                        }
                    }
                }
            }
        }
        if (line.lineTotal > BigDecimal.ZERO) {
            Text(
                "Line total: ₦${line.lineTotal.toPlainString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PickerRow(title: String, subtitle: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SelectedChip(text: String, onClear: () -> Unit) {
    Surface(
        color = DamorexColors.greenSoft,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = DamorexColors.greenDark,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onClear, modifier = Modifier.width(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.height(16.dp))
            }
        }
    }
}

internal data class DraftPurchaseLine(
    val itemId: String,
    val label: String,
    val itemCode: String? = null,
    val uomId: String,
    val quantityText: String = "1",
    val costText: String = "",
) {
    val quantity: BigDecimal get() = quantityText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val unitCost: BigDecimal get() = costText.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val lineTotal: BigDecimal get() = quantity.multiply(unitCost)
}
