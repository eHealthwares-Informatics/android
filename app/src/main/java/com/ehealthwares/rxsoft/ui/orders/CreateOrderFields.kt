package com.ehealthwares.rxsoft.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSecondaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTextField
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState

/**
 * Single combined "IT, GD" entry field for an order line — mirrors the New
 * Item screen's search-as-you-type pattern. Typing searches the catalog and
 * generic products at once; picking a suggestion fills the corresponding
 * slot (item id or generic code). If nothing is picked, the typed text is
 * submitted as the free-text name.
 */
@Composable
internal fun CreateOrderLineFields(
    query: String,
    searching: Boolean,
    searchResults: List<OrderSearchHit>,
    selItemId: String?,
    selItemLabel: String,
    pickedGenericCode: String?,
    pickedGenericName: String,
    lines: List<DraftOrderLine>,
    qtyText: String,
    priceText: String,
    createState: UiState<*>,
    onQueryChange: (String) -> Unit,
    onPickCatalog: (OrderSearchHit.Catalog) -> Unit,
    onPickGeneric: (OrderSearchHit.Generic) -> Unit,
    onQtyChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onAddLine: () -> Unit,
    onRemoveLine: (DraftOrderLine) -> Unit,
) {
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        label = "IT, GD",
        placeholder = "Item / generic, or free text",
        singleLine = true,
    )

    when {
        selItemId != null -> Text(
            "Selected: $selItemLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        pickedGenericCode != null -> Text(
            "Generic: $pickedGenericName ($pickedGenericCode)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    if (selItemId == null && pickedGenericCode == null && query.trim().length >= 2) {
        if (searching) {
            Text("Searching...", style = MaterialTheme.typography.bodySmall)
        } else {
            if (searchResults.isEmpty()) {
                Text(
                    "No match — the text will be added as free text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                searchResults.take(6).forEach { hit ->
                    val alreadyInLines = when (hit) {
                        is OrderSearchHit.Catalog -> lines.any { it.itemId == hit.item.itemId }
                        is OrderSearchHit.Generic -> lines.any { it.genericItemCode == hit.product.code }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !alreadyInLines,
                            ) {
                                when (hit) {
                                    is OrderSearchHit.Catalog -> onPickCatalog(hit)
                                    is OrderSearchHit.Generic -> onPickGeneric(hit)
                                }
                            }
                            .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (hit) {
                                    is OrderSearchHit.Catalog -> hit.item.displayName ?: hit.item.name
                                    is OrderSearchHit.Generic -> hit.product.name
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = when (hit) {
                                    is OrderSearchHit.Catalog -> "${hit.item.code ?: "catalog"} · item"
                                    is OrderSearchHit.Generic -> "${hit.product.code} · generic"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (alreadyInLines) {
                            Text(
                                "Added",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
        AppTextField(
            value = qtyText,
            onValueChange = onQtyChange,
            modifier = Modifier.weight(1f),
            label = "Quantity",
            keyboardType = KeyboardType.Number,
            singleLine = true,
        )
        AppTextField(
            value = priceText,
            onValueChange = onPriceChange,
            modifier = Modifier.weight(1f),
            label = "Unit price",
            keyboardType = KeyboardType.Decimal,
            singleLine = true,
        )
    }
    AppSecondaryButton(
        text = "Add line",
        onClick = onAddLine,
        enabled = selItemId != null || pickedGenericCode != null || query.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )
    androidx.compose.material3.HorizontalDivider()
    Text("Lines (${lines.size})", style = MaterialTheme.typography.titleSmall)
    if (lines.isEmpty()) {
        Text(
            "Search an item or generic and pick it, or just type a name to add it as free text.",
            style = MaterialTheme.typography.bodySmall,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
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
