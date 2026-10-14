package com.ehealthwares.rxsoft.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.remote.dto.StockBalanceDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.components.isListThemeDark
import com.ehealthwares.rxsoft.ui.designsystem.components.rememberQuantityFormat
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState
import java.math.BigDecimal

private val categoryChips = listOf(
    "" to "All",
    "PHARM" to "Pharmaceutical",
    "GEN" to "General",
    "SURG" to "Surgical",
    "COSM" to "Cosmetic",
    "OTH" to "Other",
)

private const val ALL_LOCATIONS = "All Locations"

@Composable
fun StockBalanceScreen(
    onAdjustmentClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    viewModel: StockBalanceViewModel = hiltViewModel(),
) {
    val stockState by viewModel.stockBalances.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var selectedLocation by rememberSaveable { mutableStateOf(ALL_LOCATIONS) }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedLocationId = locations.firstOrNull { it.name == selectedLocation }?.id

    // Debounced server-side filtering (search + category + location).
    LaunchedEffect(searchQuery, selectedCategory, selectedLocationId) {
        kotlinx.coroutines.delay(400)
        viewModel.loadStockBalances(
            search = searchQuery.ifBlank { null },
            categoryCode = selectedCategory.ifBlank { null },
            locationId = selectedLocationId,
        )
    }

    val subtitle = if (selectedLocation == ALL_LOCATIONS) "All Locations" else selectedLocation

    ListScreenTemplate(
        title = "Stock Balances",
        state = stockState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        subtitle = subtitle,
        headerIcon = Icons.Outlined.Inventory2,
        onSearchClick = { searchOpen = !searchOpen },
        filters = {
            Column {
                AnimatedVisibility(visible = searchOpen) {
                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search products by name or code",
                        modifier = Modifier.padding(bottom = SpacingTokens.xs),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                    FilterChipRow(
                        options = listOf(ALL_LOCATIONS) + locations.map { it.name },
                        selected = selectedLocation,
                        onSelect = { selectedLocation = it },
                    )
                    FilterChipRow(
                        options = categoryChips.map { it.second },
                        selected = categoryChips.firstOrNull { it.first == selectedCategory }?.second,
                        onSelect = { label ->
                            selectedCategory = categoryChips.firstOrNull { it.second == label }?.first ?: ""
                        },
                    )
                }
            }
        },
        onRefresh = {
            viewModel.loadStockBalances(
                search = searchQuery.ifBlank { null },
                categoryCode = selectedCategory.ifBlank { null },
                locationId = selectedLocationId,
            )
        },
        onLoadMore = { viewModel.loadMoreStockBalances() },
        emptyTitle = "No stock balances",
        emptySubtitle = if (searchQuery.isNotBlank() || selectedCategory.isNotBlank() || selectedLocation != ALL_LOCATIONS) {
            "No items match your filters"
        } else {
            "Stock items will appear here once added"
        },
        fab = {
            ExtendedFloatingActionButton(
                onClick = onAdjustmentClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Adjust") },
            )
        },
    ) { _ ->
        items((stockState as? UiState.Success)?.data.orEmpty(), key = { it.id }) { stock ->
            StockAccordionCard(
                stock = stock,
                expanded = expandedId == stock.id,
                onToggle = { expandedId = if (expandedId == stock.id) null else stock.id },
            )
        }
    }
}

private fun stockStatusLabel(stock: StockBalanceDto): String =
    when {
        stock.quantityOnHand.compareTo(BigDecimal.ZERO) <= 0 -> "out_of_stock"
        stock.reorderMinQty != null && stock.quantityOnHand <= stock.reorderMinQty -> "low_stock"
        else -> "in_stock"
    }

@Composable
private fun StockAccordionCard(
    stock: StockBalanceDto,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val qtyFormat = rememberQuantityFormat()
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    AccordionListCard(
        icon = Icons.Default.LocationOn,
        title = stock.item?.name ?: "Unknown item",
        dateLabel = stock.item?.code?.let { "Code: $it" },
        metaLabel = listOfNotNull(
            stock.location?.name,
            stock.lot?.let { "Lot ${it.code}" },
        ).joinToString(" · ").ifBlank { null },
        statusText = stockStatusLabel(stock),
        amount = stock.quantityOnHand,
        amountFormat = qtyFormat,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            DetailRow("Barcode", stock.item?.barcode, titleColor, mutedColor)
            DetailRow("Category", stock.item?.category?.name, titleColor, mutedColor)
            DetailRow("Base UOM", stock.item?.baseUom?.name, titleColor, mutedColor)
            DetailRow("Location", stock.location?.name, titleColor, mutedColor)
            DetailRow("Lot", stock.lot?.code, titleColor, mutedColor)
            DetailRow("Reserved", qtyFormat.format(stock.quantityReserved), titleColor, mutedColor)
            stock.averageCost?.let { DetailRow("Avg cost", qtyFormat.format(it), titleColor, mutedColor) }
            stock.reorderMinQty?.let { DetailRow("Reorder min", qtyFormat.format(it), titleColor, mutedColor) }
            stock.reorderMaxQty?.let { DetailRow("Reorder max", qtyFormat.format(it), titleColor, mutedColor) }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
    titleColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color,
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
