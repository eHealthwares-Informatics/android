package com.rxsoft.mobile.ui.analytics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.PurchasesAnalytics
import com.rxsoft.mobile.data.remote.dto.SalesAnalytics
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.components.BarChartData
import com.rxsoft.mobile.ui.designsystem.components.DateRangeFilterRow
import com.rxsoft.mobile.ui.designsystem.components.SimpleBarChart
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val tabLabels = listOf("Sales", "Purchases", "Orders")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onMenuClick: (() -> Unit)? = null,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val salesState by viewModel.salesAnalytics.collectAsState()
    val purchasesState by viewModel.purchasesAnalytics.collectAsState()
    val ordersState by viewModel.orderAnalytics.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()

    // Collect date state so UI recomposes on change
    val fromDateStr by viewModel.fromDate.collectAsState()
    val toDateStr by viewModel.toDate.collectAsState()

    // Parse current date strings for the pickers
    val fmt = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val fromLocalDate = remember(fromDateStr) {
        fromDateStr?.let { LocalDate.parse(it, fmt) }
    }
    val toLocalDate = remember(toDateStr) {
        toDateStr?.let { LocalDate.parse(it, fmt) }
    }

    // Show snackbar when export finishes
    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        AppTopAppBar(
            title = "Analytics",
            onMenuClick = onMenuClick,
            actions = {
                IconButton(
                    onClick = { viewModel.exportCsv(context) },
                    enabled = !isExporting,
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                }
                IconButton(
                    onClick = { viewModel.exportPdf(context) },
                    enabled = !isExporting,
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                }
            },
        )

        DateRangeFilterRow(
            fromDate = viewModel.fromDisplay,
            toDate = viewModel.toDisplay,
            onFromDateClick = { showFromPicker = true },
            onToDateClick = { showToPicker = true },
            onClear = { viewModel.clearDateRange() },
            onQuickSelect = { from, to -> viewModel.setDateRange(from, to) },
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            tabLabels.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        isRefreshing = false
                    },
                    text = { Text(label) },
                )
            }
        }

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh(selectedTab)
            },
        ) {
            when (selectedTab) {
                0 -> SalesAnalyticsTab(salesState)
                1 -> PurchasesAnalyticsTab(purchasesState)
                2 -> OrdersAnalyticsTab(ordersState)
            }
        }
    }
    } // Scaffold

    // ── From-date picker ───────────────────────────────────────────────────
    if (showFromPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = fromLocalDate?.let {
                it.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.setDateRange(picked, toLocalDate)
                    }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    // ── To-date picker ─────────────────────────────────────────────────────
    if (showToPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = toLocalDate?.let {
                it.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.setDateRange(fromLocalDate, picked)
                    }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
// ═══════════════════════════════════════════════════════════════════════════
// Sales tab
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SalesAnalyticsTab(state: UiState<SalesAnalytics>) {
    when (state) {
        is UiState.Idle, is UiState.Loading -> AppLoadingState()
        is UiState.Error -> AppErrorState(message = state.message)
        is UiState.Success -> {
            val data = state.data
            if (data.summary == null) {
                AppEmptyState(title = "No sales data", subtitle = "Analytics will appear once sales are made")
                return
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Summary cards
                item { SalesSummaryCard(data) }

                // Daily trend
                val trend = data.trend.orEmpty()
                if (trend.isNotEmpty()) {
                    item {
                        val trendData = remember(trend) {
                            trend.map { BarChartData(label = it.day.takeLast(5), value = it.revenue) }
                        }
                        SimpleBarChart(
                            data = trendData,
                            title = "Daily Revenue Trend",
                            barColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                    items(trend, key = { it.day }) { point ->
                        TrendRow(day = point.day, value = point.revenue, count = point.orders, label = "sales")
                    }
                }

                // By category
                val cats = data.byCategory.orEmpty()
                if (cats.isNotEmpty()) {
                    item {
                        Text("By Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(cats, key = { it.code }) { cat ->
                        CategoryRow(name = cat.name, value = cat.revenue, pct = cat.pct)
                    }
                }

                // By location
                val locs = data.byLocation.orEmpty()
                if (locs.isNotEmpty()) {
                    item {
                        Text("By Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(locs, key = { it.stockLocationId ?: it.name ?: it.hashCode().toString() }) { loc ->
                        LocationRow(name = loc.name ?: "-", value = loc.revenue)
                    }
                }

                item { Spacer(Modifier.height(SpacingTokens.md)) }
            }
        }
    }
}

@Composable
private fun SalesSummaryCard(data: SalesAnalytics) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    val s = data.summary!!
    AppCard {
        Text("Sales Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(SpacingTokens.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Revenue", style = MaterialTheme.typography.bodySmall)
                Text(format.format(s.totalRevenue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Transactions", style = MaterialTheme.typography.bodySmall)
                Text(s.totalSales.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(SpacingTokens.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Avg Order", style = MaterialTheme.typography.bodySmall)
                Text(format.format(s.averageOrderValue), fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Items Sold", style = MaterialTheme.typography.bodySmall)
                Text(s.itemsSold.toString(), fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Purchases tab
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun PurchasesAnalyticsTab(state: UiState<PurchasesAnalytics>) {
    when (state) {
        is UiState.Idle, is UiState.Loading -> AppLoadingState()
        is UiState.Error -> AppErrorState(message = state.message)
        is UiState.Success -> {
            val data = state.data
            if (data.summary == null) {
                AppEmptyState(title = "No purchase data", subtitle = "Analytics will appear once purchases are made")
                return
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                item { PurchasesSummaryCard(data) }

                val trend = data.trend.orEmpty()
                if (trend.isNotEmpty()) {
                    item {
                        val trendData = remember(trend) {
                            trend.map { BarChartData(label = it.day.takeLast(5), value = it.value) }
                        }
                        SimpleBarChart(
                            data = trendData,
                            title = "Daily Cost Trend",
                            barColor = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    items(trend, key = { it.day }) { point ->
                        TrendRow(day = point.day, value = point.value, count = point.orders, label = "purchases")
                    }
                }

                val bySupplier = data.bySupplier.orEmpty()
                if (bySupplier.isNotEmpty()) {
                    item {
                        Text("By Supplier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(bySupplier, key = { it.supplierId ?: it.name ?: it.hashCode().toString() }) { sup ->
                        SupplierRow(name = sup.name ?: "-", cost = sup.value)
                    }
                }

                val cats = data.byCategory.orEmpty()
                if (cats.isNotEmpty()) {
                    item {
                        Text("By Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(cats, key = { it.code ?: it.name ?: it.hashCode().toString() }) { cat ->
                        CategoryRow(name = cat.name ?: "-", value = cat.value, pct = cat.pct)
                    }
                }

                val recent = data.recent.orEmpty()
                if (recent.isNotEmpty()) {
                    item {
                        Text("Recent Purchases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(recent, key = { it.id ?: it.purchaseOrderNumber ?: it.hashCode().toString() }) { purchase ->
                        AppCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(purchase.purchaseOrderNumber ?: "-", fontWeight = FontWeight.Medium)
                                StatusBadge(purchase.status ?: "-")
                            }
                            Text(
                                "${purchase.supplierName ?: "-"} · ${formatNaira(purchase.totalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            purchase.orderDate?.let {
                                Text(it.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(SpacingTokens.md)) }
            }
        }
    }
}

@Composable
private fun PurchasesSummaryCard(data: PurchasesAnalytics) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    val s = data.summary!!
    AppCard {
        Text("Purchases Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(SpacingTokens.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Cost", style = MaterialTheme.typography.bodySmall)
                Text(format.format(s.totalValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Orders", style = MaterialTheme.typography.bodySmall)
                Text(s.totalPOs.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(SpacingTokens.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Avg Order", style = MaterialTheme.typography.bodySmall)
                Text(format.format(s.averagePOValue), fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Items Bought", style = MaterialTheme.typography.bodySmall)
                Text(s.itemsPurchased.toString(), fontWeight = FontWeight.Medium)
            }
        }
        s.topSupplier?.let { sup ->
            Spacer(Modifier.height(SpacingTokens.sm))
            HorizontalDivider()
            Spacer(Modifier.height(SpacingTokens.sm))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Top Supplier", style = MaterialTheme.typography.bodySmall)
                Text(sup.name ?: "-", fontWeight = FontWeight.Medium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Top Supplier Spend", style = MaterialTheme.typography.bodySmall)
                Text(format.format(sup.value), fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Orders tab
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun OrdersAnalyticsTab(state: UiState<OrderAnalyticsSummary>) {
    when (state) {
        is UiState.Idle, is UiState.Loading -> AppLoadingState()
        is UiState.Error -> AppErrorState(message = state.message)
        is UiState.Success -> {
            val data = state.data
            if (data.totalOrders == 0) {
                AppEmptyState(title = "No orders yet", subtitle = "Order analytics will appear here once orders are created")
                return
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                item { OrdersSummaryCard(data) }

                item {
                    Text("Recent Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(data.recentOrders, key = { it.id }) { order ->
                    AppCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.orderNumber, fontWeight = FontWeight.Medium)
                            StatusBadge(order.orderStatus)
                        }
                        Text(
                            "${order.items?.size ?: 0} item(s) · ${formatNaira(order.totalAmount.toDouble())}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        order.createdAt?.let {
                            Text(it.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item { Spacer(Modifier.height(SpacingTokens.md)) }
            }
        }
    }
}

@Composable
private fun OrdersSummaryCard(data: OrderAnalyticsSummary) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    AppCard {
        Text("Orders Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(SpacingTokens.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Orders", style = MaterialTheme.typography.bodySmall)
                Text(data.totalOrders.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Value", style = MaterialTheme.typography.bodySmall)
                Text(format.format(data.totalValue), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(SpacingTokens.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatusCountChip(label = "Pending", count = data.pendingOrders, color = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(SpacingTokens.sm))
            StatusCountChip(label = "Posted", count = data.postedOrders, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(SpacingTokens.sm))
            StatusCountChip(label = "Cancelled", count = data.cancelledOrders, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shared row helpers
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun TrendRow(day: String, value: Double, count: Int, label: String) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(day, style = MaterialTheme.typography.bodyMedium)
            Text(format.format(value), fontWeight = FontWeight.Medium)
        }
        Text("$count $label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CategoryRow(name: String, value: Double, pct: Double) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(format.format(value), fontWeight = FontWeight.Medium)
        }
        Text("${pct}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LocationRow(name: String, value: Double) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(format.format(value), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SupplierRow(name: String, cost: Double) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(format.format(cost), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "pending", "draft" -> MaterialTheme.colorScheme.tertiary
        "posted", "approved", "completed", "sale", "received" -> MaterialTheme.colorScheme.primary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = status.replace("_", " ").uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun StatusCountChip(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatNaira(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    return format.format(value)
}
