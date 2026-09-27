package com.rxsoft.mobile.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.items
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.util.UiState

@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onAddItem: () -> Unit = {},
    onEditItem: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    val itemsState by viewModel.items.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    ListScreenTemplate(
        title = "Items",
        state = itemsState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        searchQuery = "",
        onSearchQueryChange = { viewModel.loadItems(it.ifBlank { null }) },
        onRefresh = { viewModel.loadItems() },
        onLoadMore = { viewModel.loadMoreItems() },
        emptyTitle = "No items found",
        emptySubtitle = "Items will appear here once added",
        fab = {
            FloatingActionButton(
                onClick = onAddItem,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
    ) { data ->
        items(data, key = { it.id }) { item ->
            ItemCard(item, onClick = { onEditItem(item.id) })
        }
    }
}

@Composable
private fun ItemCard(item: ItemDto, onClick: () -> Unit) {
    AppCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(item.name, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(com.rxsoft.mobile.ui.designsystem.token.SpacingTokens.lg)) {
            Text("Code: ${item.code ?: "-"}", style = MaterialTheme.typography.bodySmall)
            item.barcode?.let { Text("Barcode: $it", style = MaterialTheme.typography.bodySmall) }
        }
        item.category?.name?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
