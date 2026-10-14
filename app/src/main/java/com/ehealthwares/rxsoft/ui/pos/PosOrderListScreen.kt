package com.ehealthwares.rxsoft.ui.pos

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.ehealthwares.rxsoft.data.remote.dto.SaleDto
import com.ehealthwares.rxsoft.data.remote.dto.SaleLineDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionItemsHeader
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionTotalRow
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.components.rememberNairaFormat
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.ui.designsystem.components.isListThemeDark
import com.ehealthwares.rxsoft.ui.designsystem.theme.AppThemeColors
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.ui.designsystem.theme.AppearanceMode
import com.ehealthwares.rxsoft.ui.designsystem.theme.ThemeViewModel
import com.ehealthwares.rxsoft.util.PosConfigManager
import com.ehealthwares.rxsoft.util.UiState
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val SALES_FILTERS = listOf("All Sales", "Today", "This Week", "This Month", "Custom")

@Composable
fun PosOrderListScreen(
    posConfigManager: PosConfigManager,
    onNewSale: () -> Unit = {},
    onSaleClick: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    onListScrollDirection: ((scrollingDown: Boolean) -> Unit)? = null,
    viewModel: PosOrderListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    themeViewModel: ThemeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val salesState by viewModel.sales.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val saleDetails by viewModel.saleDetails.collectAsState()
    val config by posConfigManager.config.collectAsState()
    val configState by posConfigManager.configState.collectAsState()
    val themeSettings by themeViewModel.themeSettings.collectAsState()
    val scope = rememberCoroutineScope()

    val isDark = when (themeSettings.appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> {
            val config2 = androidx.compose.ui.platform.LocalContext.current.resources.configuration
            val uiMode = config2.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }

    // Accordion + filter state
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(SALES_FILTERS.first()) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customFrom by rememberSaveable { mutableStateOf("") }
    var customTo by rememberSaveable { mutableStateOf("") }

    val allSales = (salesState as? UiState.Success)?.data.orEmpty()
    val visibleSales = remember(allSales, selectedChip, searchText, customFrom, customTo) {
        allSales.filter { sale ->
            matchesDateFilter(sale, selectedChip, customFrom, customTo) && matchesSearch(sale, searchText)
        }
    }

    ListScreenTemplate(
        title = "POS Sales",
        state = salesState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        onRefresh = { viewModel.loadSales() },
        onLoadMore = {
            // Infinite scroll: only auto-fetch further pages when no filter is
            // hiding rows, or a full screen of matches is already visible —
            // otherwise a narrow filter (e.g. "Today") would walk every page.
            val filtering = selectedChip != "All Sales" || searchText.isNotBlank()
            if (!filtering || visibleSales.size >= 10) viewModel.loadMoreSales()
        },
        onListScrollDirection = onListScrollDirection,
        subtitle = config?.stockLocation?.name?.takeIf { it.isNotBlank() } ?: "All Branches",
        headerIcon = Icons.Outlined.Storefront,
        onSearchClick = { searchOpen = !searchOpen },
        onAddClick = {
            // Refetch + save the POS config first when the stock location
            // hasn't been resolved yet, then start the sale.
            scope.launch {
                var cfg = posConfigManager.config.value
                if (cfg?.stockLocation == null) {
                    cfg = posConfigManager.fetchConfig()
                }
                if (cfg?.stockLocation != null) {
                    onNewSale()
                } else {
                    Log.e("PosOrderList", "Cannot start sale: stock location not configured")
                }
            }
        },
        addDescription = "New Sale",
        filters = {
            Column {
                AnimatedVisibility(visible = searchOpen) {
                    AppSearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it },
                        placeholder = "Search sale no., cashier, amount",
                        modifier = Modifier.padding(bottom = SpacingTokens.xs),
                    )
                }
                FilterChipRow(
                    options = SALES_FILTERS,
                    selected = selectedChip,
                    onSelect = {
                        if (it == "Custom") showCustomDialog = true else selectedChip = it
                    },
                )
            }
        },
        emptyTitle = "No orders yet",
        emptySubtitle = "Create a new sale to get started",
        topBarActions = {
            IconButton(onClick = {
                val newMode = if (isDark) AppearanceMode.LIGHT else AppearanceMode.DARK
                themeViewModel.updateAppearanceMode(newMode)
            }) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        configMissing = configState is UiState.Error || (configState is UiState.Success && config?.stockLocation == null),
        configMessage = when {
            configState is UiState.Error -> (configState as UiState.Error).message
            configState is UiState.Success && config?.stockLocation == null -> "Stock location not configured for this user. Go to Settings to check your POS configuration."
            else -> null
        },
    ) { _ ->
        if (visibleSales.isEmpty() && allSales.isNotEmpty()) {
            item {
                Text(
                    "No sales match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(SpacingTokens.lg),
                )
            }
        }
        items(visibleSales, key = { it.id }) { sale ->
            SaleAccordionCard(
                sale = sale,
                detailState = saleDetails[sale.id],
                expanded = expandedId == sale.id,
                onToggle = {
                    expandedId = if (expandedId == sale.id) null else sale.id
                },
                onOpenDetails = { onSaleClick(sale.id) },
                onLoadDetail = { viewModel.loadSaleDetail(sale.id) },
            )
        }
    }

    if (showCustomDialog) {
        CustomRangeDialog(
            initialFrom = customFrom,
            initialTo = customTo,
            onApply = { from, to ->
                customFrom = from
                customTo = to
                selectedChip = "Custom"
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────

@Composable
private fun SaleAccordionCard(
    sale: SaleDto,
    detailState: UiState<SaleDto>?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenDetails: () -> Unit,
    onLoadDetail: () -> Unit,
) {
    // Lines may already ride on the list response; otherwise fetch on expand.
    LaunchedEffect(expanded, sale.id) {
        if (expanded && sale.lines == null && detailState == null) {
            onLoadDetail()
        }
    }

    val lines: List<SaleLineDto>? = sale.lines
        ?: (detailState as? UiState.Success)?.data?.lines

    AccordionListCard(
        icon = Icons.Outlined.ReceiptLong,
        title = sale.saleNumber,
        dateLabel = com.ehealthwares.rxsoft.ui.designsystem.components.formatListTimestamp(sale.saleDate),
        metaLabel = "Cashier: ${sale.soldBy?.username ?: sale.customer?.name ?: "Walk-in"}",
        statusText = sale.status,
        amount = sale.totalAmount,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        AccordionItemsHeader(label = "Items (${lines?.size ?: 0})")
        when {
            lines != null -> {
                lines.forEach { line -> SaleItemRow(line = line) }
                Spacer(Modifier.height(SpacingTokens.sm))
                AccordionTotalRow(
                    label = "Total Amount",
                    amount = sale.totalAmount,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onOpenDetails) { Text("View details") }
                }
            }
            detailState is UiState.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading items…", style = MaterialTheme.typography.bodySmall)
                }
            }
            detailState is UiState.Error -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        detailState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TextButton(onClick = onLoadDetail) { Text("Retry") }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun SaleItemRow(line: SaleLineDto) {
    val format = rememberNairaFormat()
    val item = line.item
    val isDark = isListThemeDark()
    val thumbBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else AppThemeColors.current.mint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(thumbBg),
            contentAlignment = Alignment.Center,
        ) {
            val url = item?.smallImageUrl ?: item?.mediumImageUrl ?: item?.imageUrl
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = AppThemeColors.current.greenDark,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item?.name ?: "Item",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitle = item?.code ?: item?.barcode
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = format.format(line.unitPrice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "× ${stripQty(line.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = format.format(line.lineTotal),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Filters ───────────────────────────────────────────────────────────────────

private fun stripQty(q: BigDecimal): String =
    q.stripTrailingZeros().toPlainString()

private fun matchesSearch(sale: SaleDto, needle: String): Boolean {
    if (needle.isBlank()) return true
    val n = needle.trim().lowercase()
    return sale.saleNumber.lowercase().contains(n) ||
        (sale.customer?.name ?: "").lowercase().contains(n) ||
        (sale.soldBy?.username ?: "").lowercase().contains(n) ||
        sale.totalAmount.toPlainString().contains(n)
}

private fun matchesDateFilter(sale: SaleDto, chip: String, from: String, to: String): Boolean {
    val date = parseSaleDate(sale.saleDate) ?: return chip == "All Sales"
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return when (chip) {
        "All Sales" -> true
        "Today" -> !date.before(today.time)
        "This Week" -> {
            val weekStart = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
            val weekEnd = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
            !date.before(weekStart.time) && date.before(weekEnd.time)
        }
        "This Month" -> {
            val monthStart = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            val monthEnd = (monthStart.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            !date.before(monthStart.time) && date.before(monthEnd.time)
        }
        "Custom" -> {
            val start = parseRangeDate(from)?.let { d ->
                val c = Calendar.getInstance(); c.time = d
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); c.time
            }
            val end = parseRangeDate(to)?.let { d ->
                val c = Calendar.getInstance(); c.time = d
                c.add(Calendar.DAY_OF_YEAR, 1)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); c.time
            }
            (start == null || !date.before(start)) && (end == null || date.before(end))
        }
        else -> true
    }
}

private fun parseSaleDate(iso: String?): java.util.Date? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val cleaned = iso.substringBefore('.').replace("Z", "").replace(" ", "T")
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(cleaned)
    }.getOrNull()
}

private fun parseRangeDate(text: String): java.util.Date? {
    if (text.isBlank()) return null
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(text.trim())
    }.getOrNull()
}

@Composable
private fun CustomRangeDialog(
    initialFrom: String,
    initialTo: String,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var from by remember { mutableStateOf(initialFrom) }
    var to by remember { mutableStateOf(initialTo) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom range") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                Text(
                    "Enter dates as yyyy-MM-dd. Leave a field empty to unbound the range.",
                    style = MaterialTheme.typography.bodySmall,
                )
                androidx.compose.material3.OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text("From") },
                    placeholder = { Text("2026-09-01") },
                    singleLine = true,
                )
                androidx.compose.material3.OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To") },
                    placeholder = { Text("2026-09-30") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(from, to) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
