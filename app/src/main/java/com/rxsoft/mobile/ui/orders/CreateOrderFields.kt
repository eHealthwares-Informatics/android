package com.rxsoft.mobile.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.rxsoft.mobile.ui.designsystem.components.AppSecondaryButton
import com.rxsoft.mobile.util.UiState

@Composable
internal fun CreateOrderCascadeFields(
    selItemId: String?,
    selItemLabel: String,
    itemQuery: String,
    searching: Boolean,
    searchResults: List<OrderSearchHit>,
    lines: List<DraftOrderLine>,
    genericQuery: String,
    freetextName: String,
    qtyText: String,
    priceText: String,
    createState: UiState<*>,
    onPickItem: (OrderSearchHit.Catalog) -> Unit,
    onGenericChange: (String) -> Unit,
    onPickGeneric: (OrderSearchHit.Generic) -> Unit,
    onFreetextChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAddLine: () -> Unit,
    onRemoveLine: (DraftOrderLine) -> Unit,
) {
    if (selItemId != null) {
        Text("Selected: $selItemLabel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    } else if (itemQuery.trim().length >= 2) {
        if (searching) {
            Text("Searching...", style = MaterialTheme.typography.bodySmall)
        } else {
            val hits = searchResults.filterIsInstance<OrderSearchHit.Catalog>()
            if (hits.isEmpty()) Text("No catalog match - use generic or free text below.", style = MaterialTheme.typography.bodySmall)
            hits.take(5).forEach { hit ->
                TextButton(onClick = { onPickItem(hit) }, enabled = lines.none { it.itemId == hit.item.itemId }) {
                    Text("${hit.item.displayName ?: hit.item.name} - ${hit.item.code ?: "catalog"}", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    OutlinedTextField(
        value = genericQuery,
        onValueChange = onGenericChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("2 - Generic product (code or name)") },
        singleLine = true,
    )
    if (selItemId == null && genericQuery.trim().length >= 2 && !searching) {
        searchResults.filterIsInstance<OrderSearchHit.Generic>().take(5).forEach { hit ->
            TextButton(onClick = { onPickGeneric(hit) }, enabled = lines.none { it.genericItemCode == hit.product.code }) {
                Text("${hit.product.name} - ${hit.product.code}")
            }
        }
    }
    OutlinedTextField(
        value = freetextName,
        onValueChange = onFreetextChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("3 - Free text (item name)") },
        singleLine = true,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(com.rxsoft.mobile.ui.designsystem.token.SpacingTokens.sm)) {
        OutlinedTextField(
            value = qtyText, onValueChange = onQtyChange, modifier = Modifier.weight(1f),
            label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
        )
        OutlinedTextField(
            value = priceText, onValueChange = onPriceChange, modifier = Modifier.weight(1f),
            label = { Text("Unit price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
        )
    }
    AppSecondaryButton(
        text = "Add line", onClick = onAddLine,
        enabled = selItemId != null || genericQuery.isNotBlank() || freetextName.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    androidx.compose.material3.HorizontalDivider()
    Text("Lines (${lines.size})", style = MaterialTheme.typography.titleSmall)
    if (lines.isEmpty()) {
        Text("Pick an item (auto-fills generic + free text), or enter generic / free text.", style = MaterialTheme.typography.bodySmall)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(com.rxsoft.mobile.ui.designsystem.token.SpacingTokens.xs)) {
            lines.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${line.quantity} x ${line.label}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRemoveLine(line) }) { Text("Remove") }
                }
            }
        }
    }
    val err = createState as? UiState.Error
    if (err != null) Text(err.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}
