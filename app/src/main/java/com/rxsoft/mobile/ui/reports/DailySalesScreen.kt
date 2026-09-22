package com.rxsoft.mobile.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
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
import com.rxsoft.mobile.data.remote.dto.DailySalesRow
import com.rxsoft.mobile.data.remote.dto.TopSellingItem
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.components.DateRangeFilterRow
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySalesScreen(
    onMenuClick: (() -> Unit)? = null,
    viewModel: DailySalesViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val reportState by viewModel.dailyRows.collectAsState()
    val topItemsState by viewModel.topItems.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    val fromDateStr by viewModel.fromDate.collectAsState()
    val toDateStr by viewModel.toDate.collectAsState()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val fromLocalDate = remember(fromDateStr) { fromDateStr?.let { LocalDate.parse(it, fmt) } }
    val toLocalDate = remember(toDateStr) { toDateStr?.let { LocalDate.parse(it, fmt) } }

    LaunchedEffect(reportState) {
        if (reportState !is UiState.Loading) isRefreshing = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopAppBar(title = "Reports", onMenuClick = onMenuClick)

        DateRangeFilterRow(
            fromDate = viewModel.fromDisplay,
            toDate = viewModel.toDisplay,
            onFromDateClick = { showFromPicker = true },
            onToDateClick = { showToPicker = true },
            onClear = { viewModel.clearDateRange() },
            onQuickSelect = { from, to -> viewModel.setDateRange(from, to) },
        )

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadReport()
            },
        ) {
            when (val state = reportState) {
                is UiState.Idle, is UiState.Loading -> {
                    AppLoadingState()
                }
                is UiState.Error -> {
                    AppErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadReport() },
                    )
                }
                is UiState.Success -> {
                    if (state.data.isEmpty() && topItemsState !is UiState.Success) {
                        AppEmptyState(title = "No sales data", subtitle = "Sales data will appear here once transactions")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                        ) {
                            item { ReportSummaryCard(state.data) }
                            item {
                                Text(
                                    "Daily Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            items(state.data, key = { it.day }) { row ->
                                DailyRowCard(row)
                            }
                            item {
                                Text(
                                    "Top Selling Items",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            when (val topState = topItemsState) {
                                is UiState.Success -> {
                                    items(topState.data, key = { it.itemCode ?: it.hashCode().toString() }) { item ->
                                        TopItemCard(item)
                                    }
                                }
                                is UiState.Loading -> {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            androidx.compose.material3.CircularProgressIndicator()
                                        }
                                    }
                                }
                                is UiState.Error -> {
                                    item {
                                        Text(topState.message, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

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
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

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
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun ReportSummaryCard(rows: List<DailySalesRow>) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    val totalAmount = rows.sumOf { it.totalAmount }
    val totalSales = rows.sumOf { it.salesCount }

    AppCard {
        Text("Daily Sales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(SpacingTokens.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Revenue", style = MaterialTheme.typography.bodySmall)
                Text(
                    format.format(totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Transactions", style = MaterialTheme.typography.bodySmall)
                Text(
                    totalSales.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DailyRowCard(row: DailySalesRow) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.day, fontWeight = FontWeight.Medium)
                Text("${row.salesCount} sale(s)", style = MaterialTheme.typography.bodySmall)
            }
            Text(format.format(row.totalAmount), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TopItemCard(item: TopSellingItem) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.itemCode ?: "Unknown", fontWeight = FontWeight.Medium)
                Text("Qty: ${item.quantitySold}", style = MaterialTheme.typography.bodySmall)
            }
            Text(format.format(item.revenue), fontWeight = FontWeight.Bold)
        }
    }
}
