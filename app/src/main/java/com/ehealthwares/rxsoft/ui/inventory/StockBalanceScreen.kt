package com.rxsoft.mobile.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.rxsoft.mobile.data.remote.dto.StockBalanceDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.math.BigDecimal
import java.text.NumberFormat

private val categoryChips = listOf(
    "" to "All",
    "PHARM" to "Pharmaceutical",
    "GEN" to "General",
    "SURG" to "Surgical",
    "COSM" to "Cosmetic",
    "OTH" to "Other",
)

@Composable
fun StockBalanceScreen(
    onAdjustmentClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    viewModel: StockBalanceViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val stockState by viewModel.stockBalances.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    // Debounced search
    LaunchedEffect(searchQuery, selectedCategory) {
        kotlinx.coroutines.delay(400)
        viewModel.loadStockBalances(
            search = searchQuery.ifBlank { null },
            categoryCode = selectedCategory.ifBlank { null },
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        AppTopAppBar(title = "Stock Balances", onMenuClick = onMenuClick)

        // ── Search bar ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
            label = { Text("Search products by name or code") },
            singleLine = true,
        )

        // ── Category filter chips ───────────────────────────────────────────
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            items(categoryChips.size) { index ->
                val (code, label) = categoryChips[index]
                FilterChip(
                    selected = selectedCategory == code,
                    onClick = {
                        selectedCategory = code
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Spacer(Modifier.height(SpacingTokens.xs))

        // ── Content ─────────────────────────────────────────────────────────
        when (val state = stockState) {
            is UiState.Idle, is UiState.Loading -> AppLoadingState()
            is UiState.Error -> AppErrorState(
                message = state.message,
                onRetry = { viewModel.loadStockBalances(search = searchQuery.ifBlank { null }, categoryCode = selectedCategory.ifBlank { null }) },
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    AppEmptyState(
                        title = "No stock balances",
                        subtitle = if (searchQuery.isNotBlank() || selectedCategory.isNotBlank()) {
                            "No items match your filters"
                        } else {
                            "Stock items will appear here once added"
                        },
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = SpacingTokens.screenHorizontal,
                            vertical = SpacingTokens.xs,
                        ),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        items(state.data, key = { it.id }) { stock ->
                            StockCard(stock)
                        }
                        // Load more trigger
                        if (isLoadingMore) {
                            item {
                                AppLoadingState(description = "Loading more…")
                            }
                        }
                    }
                }
            }
        }

        // ── FAB ─────────────────────────────────────────────────────────────
        ExtendedFloatingActionButton(
            onClick = onAdjustmentClick,
            modifier = Modifier
                .align(Alignment.End)
                .padding(SpacingTokens.screenHorizontal),
            icon = { Icon(Icons.Default.Add, contentDescription = "Adjust stock") },
            text = { Text("Adjust") },
        )
    }
}

@Composable
private fun StockCard(stock: StockBalanceDto) {
    val format = remember { NumberFormat.getInstance() }

    AppCard {
        // ── Item name + code ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stock.item?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                stock.item?.code?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Text(
                text = format.format(stock.quantityOnHand),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (stock.quantityOnHand.toDouble() > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }

        // ── Location + lot + reserved ───────────────────────────────────────
        Spacer(Modifier.height(SpacingTokens.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stock.location?.let { loc ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.padding(end = SpacingTokens.xs),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        // size set via modifier if needed
                    )
                    Text(
                        loc.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            stock.lot?.let { lot ->
                Text(
                    "Lot: ${lot.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (stock.quantityReserved.toDouble() > 0) {
            Text(
                "Reserved: ${format.format(stock.quantityReserved)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        stock.averageCost?.let { cost ->
            if (cost.toDouble() > 0) {
                Text(
                    "Avg cost: ${format.format(cost)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Reorder levels ──────────────────────────────────────────────────
        val minQty = stock.reorderMinQty
        val maxQty = stock.reorderMaxQty
        if (minQty != null || maxQty != null) {
            Spacer(Modifier.height(SpacingTokens.xs))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                minQty?.let {
                    Text(
                        "Min: ${format.format(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                maxQty?.let {
                    Text(
                        "Max: ${format.format(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
