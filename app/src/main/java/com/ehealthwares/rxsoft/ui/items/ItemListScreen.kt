package com.ehealthwares.rxsoft.ui.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.items
import com.ehealthwares.rxsoft.data.remote.dto.ItemDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.components.isListThemeDark
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState

private const val ALL_ITEMS = "All Items"
private const val MAX_CATEGORY_CHIPS = 6

@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onAddItem: () -> Unit = {},
    onEditItem: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    val itemsState by viewModel.items.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(ALL_ITEMS) }

    val chipOptions = remember(categories) {
        listOf(ALL_ITEMS) + categories.take(MAX_CATEGORY_CHIPS).mapNotNull { it.name }
    }
    val selectedCategoryId = categories.firstOrNull { it.name == selectedChip }?.id

    val allItems = (itemsState as? UiState.Success)?.data.orEmpty()
    val visibleItems = remember(allItems, selectedCategoryId, searchText) {
        allItems.filter { item ->
            val inCategory = selectedCategoryId == null || item.category?.id == selectedCategoryId
            val n = searchText.trim().lowercase()
            val inSearch = n.isBlank() ||
                item.name.lowercase().contains(n) ||
                (item.code ?: "").lowercase().contains(n) ||
                (item.barcode ?: "").contains(n)
            inCategory && inSearch
        }
    }

    ListScreenTemplate(
        title = "Items",
        state = itemsState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        subtitle = "Catalogue",
        headerIcon = Icons.Outlined.Inventory2,
        onSearchClick = { searchOpen = !searchOpen },
        onAddClick = onAddItem,
        addDescription = "Add item",
        filters = {
            Column {
                AnimatedVisibility(visible = searchOpen) {
                    AppSearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it },
                        placeholder = "Search name, code, barcode",
                        modifier = Modifier.padding(bottom = SpacingTokens.xs),
                    )
                }
                FilterChipRow(
                    options = chipOptions,
                    selected = selectedChip,
                    onSelect = { selectedChip = it },
                )
            }
        },
        onRefresh = { viewModel.loadItems() },
        onLoadMore = {
            // Infinite scroll: don't cascade page loads while a narrow
            // category/search filter hides rows.
            val filtering = selectedChip != ALL_ITEMS || searchText.isNotBlank()
            if (!filtering || visibleItems.size >= 10) viewModel.loadMoreItems()
        },
        emptyTitle = "No items found",
        emptySubtitle = "Items will appear here once added",
    ) { _ ->
        if (visibleItems.isEmpty() && allItems.isNotEmpty()) {
            item {
                Text(
                    "No items match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(SpacingTokens.lg),
                )
            }
        }
        items(visibleItems, key = { it.id }) { item ->
            ItemAccordionCard(
                item = item,
                expanded = expandedId == item.id,
                onToggle = { expandedId = if (expandedId == item.id) null else item.id },
                onEdit = { onEditItem(item.id) },
            )
        }
    }
}

@Composable
private fun ItemAccordionCard(
    item: ItemDto,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    AccordionListCard(
        icon = Icons.Outlined.Medication,
        title = item.name,
        dateLabel = "Code: ${item.code ?: "—"}",
        metaLabel = buildString {
            item.barcode?.let { append("Barcode: $it") }
            item.category?.name?.let {
                if (isNotEmpty()) append(" · ")
                append(it)
            }
        }.ifBlank { null },
        statusText = null,
        amount = null,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            DetailRow("Barcode", item.barcode)
            DetailRow("Category", item.category?.name, titleColor, mutedColor)
            DetailRow("Base UOM", item.baseUom?.name, titleColor, mutedColor)
            DetailRow("Sale UOM", item.saleUom?.name, titleColor, mutedColor)
            DetailRow("Purchase UOM", item.purchaseUom?.name, titleColor, mutedColor)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onEdit) { Text("View / Edit") }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    mutedColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = mutedColor)
        Text(
            value ?: "—",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(180.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}
