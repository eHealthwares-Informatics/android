package com.ehealthwares.rxsoft.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.ehealthwares.rxsoft.R
import com.ehealthwares.rxsoft.data.remote.dto.ItemDto
import com.ehealthwares.rxsoft.data.remote.dto.PartyDto
import com.ehealthwares.rxsoft.data.remote.dto.PaymentMethodDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AppIconButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTextButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBarActions
import com.ehealthwares.rxsoft.ui.designsystem.theme.AppThemeColors
import com.ehealthwares.rxsoft.ui.designsystem.token.ElevationTokens
import com.ehealthwares.rxsoft.ui.designsystem.token.ShapeTokens
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.ReceiptData
import com.ehealthwares.rxsoft.util.ReceiptLine
import com.ehealthwares.rxsoft.util.UiState
import android.util.Log
import com.ehealthwares.rxsoft.util.printReceipt
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosTerminalScreen(
    onOrderCreated: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: PosTerminalViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val stockGate by viewModel.stockGate.collectAsState()
    val adjustingItemId by viewModel.adjustingItemId.collectAsState()
    val adjustError by viewModel.adjustError.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val priceLists by viewModel.priceLists.collectAsState()
    val selectedPriceListId by viewModel.selectedPriceListId.collectAsState()
    val pendingSaleCount by viewModel.pendingSaleCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val themeColors = AppThemeColors.current
    val context = LocalContext.current

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showNoStockDialog by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    var showCustomerSearch by remember { mutableStateOf(false) }
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))

    LaunchedEffect(stockGate) {
        when (stockGate) {
            is StockGate.Ready -> {
                showNoStockDialog = false
                viewModel.loadPaymentMethods()
                showPaymentDialog = true
                viewModel.consumeStockGate()
            }
            is StockGate.Missing -> showNoStockDialog = true
            else -> {}
        }
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is UiState.Success) {
            val sale = (checkoutState as UiState.Success<*>).data
            if (sale is com.ehealthwares.rxsoft.data.remote.dto.SaleDto) {
                // Offline sales live only in the outbox (id is a local
                // clientRef): keep the "saved offline" note visible instead of
                // navigating to a detail screen that doesn't exist yet.
                if (sale.status == OFFLINE_QUEUED_STATUS) return@LaunchedEffect
                // Receipt printing must never crash the POS after a successful
                // sale — failures are contained inside printReceipt (it falls
                // back to a shareable PDF when no print service exists).
                try {
                    val paid = sale.payments.orEmpty().sumOf { it.amount }
                    printReceipt(
                        context,
                        ReceiptData(
                            saleNumber = sale.saleNumber,
                            customerName = sale.customer?.name ?: selectedCustomer?.name,
                            items = sale.lines.orEmpty().map { line ->
                                ReceiptLine(
                                    line.item?.name ?: "",
                                    line.quantity,
                                    line.unitPrice,
                                    line.lineTotal
                                )
                            },
                            subtotal = sale.totalAmount,
                            total = sale.totalAmount,
                            paidAmount = paid,
                            changeAmount = (paid - sale.totalAmount).max(java.math.BigDecimal.ZERO)
                        )
                    )
                } catch (e: Exception) {
                    Log.e("PosTerminalScreen", "Receipt generation failed", e)
                }
                onOrderCreated(sale.id)
                viewModel.resetCheckoutState()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Brand-green top bar — extends under the status bar (edge-to-edge);
            // the row pads below the status-bar inset while the surface stays full-bleed
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(top = SpacingTokens.xs, bottom = SpacingTokens.sm)
                        .padding(horizontal = SpacingTokens.screenHorizontal),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppTopAppBarActions(
                        icon = Icons.Default.KeyboardArrowDown,
                        onClick = onBack,
                        description = "Back",
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.xs))
                    Image(
                        painter = painterResource(R.drawable.pharmacy_cross),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Sale",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "Find and add products to your sale",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OfflineSyncBadge(
                        pendingCount = pendingSaleCount,
                        isSyncing = isSyncing,
                        onClick = { viewModel.syncSalesNow() },
                    )
                    if (cartItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        AppTopAppBarActions(
                            icon = Icons.Default.Delete,
                            onClick = { viewModel.clearCart() },
                            description = "Clear cart",
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Decorative leaf sprig behind the content, top-end
            Image(
                painter = painterResource(R.drawable.leaf_sprig),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = SpacingTokens.xs, end = SpacingTokens.sm)
                    .size(110.dp)
                    .alpha(0.45f),
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.padding(horizontal = SpacingTokens.screenHorizontal)) {
                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            viewModel.updateSearchQuery(it)
                            showSearchResults = it.length >= 2
                        },
                        modifier = Modifier.padding(vertical = SpacingTokens.sm),
                        placeholder = "Search products...",
                        searchDescription = "Search products",
                        clearDescription = "Clear search",
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (showSearchResults && searchQuery.length >= 2) {
                    when (val results = searchResults) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(SpacingTokens.xxl))
                            }
                        }
                        is UiState.Success -> {
                            if (results.data.isNotEmpty()) {
                                androidx.compose.material3.Surface(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                    shadowElevation = ElevationTokens.sm,
                                ) {
                                    LazyColumn(
                                        contentPadding = PaddingValues(SpacingTokens.sm),
                                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                                    ) {
                                        items(results.data, key = { it.id }) { item ->
                                            ProductSearchItem(
                                                item = item,
                                                onClick = {
                                                    viewModel.addToCart(item)
                                                    showSearchResults = false
                                                    viewModel.updateSearchQuery("")
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                results.message,
                                modifier = Modifier.padding(horizontal = SpacingTokens.screenHorizontal),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        else -> {}
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(themeColors.greenSoft, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Customer",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    Text(
                        text = selectedCustomer?.name ?: "Walk-in Customer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (selectedCustomer != null) "Change" else "Select",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showCustomerSearch = true }
                            .padding(SpacingTokens.xs)
                            .semantics { contentDescription = "Select customer" },
                    )
                }

                viewModel.currentStockLocationName?.let { locName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(themeColors.greenSoft, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        Text(
                            locName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AppThemeColors.current.muted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (priceLists.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Price list:",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppThemeColors.current.muted,
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        priceLists.forEach { pl ->
                            PriceListChip(
                                label = pl.name.ifBlank { pl.code },
                                selected = selectedPriceListId == pl.id,
                                onClick = { viewModel.setPriceList(pl.id) },
                            )
                            Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        }
                    }
                }

                HorizontalDivider(color = themeColors.greenSoft)

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = themeColors.greenSoft,
                                modifier = Modifier.size(56.dp),
                            )
                            Spacer(modifier = Modifier.height(SpacingTokens.sm))
                            Text(
                                "Search and add products to start",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppThemeColors.current.muted,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(SpacingTokens.sm),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        items(cartItems, key = { it.item.id }) { cartItem ->
                            CartItemRow(
                                item = cartItem,
                                onQuantityChange = { qty ->
                                    viewModel.updateQuantity(cartItem.item.id, qty)
                                },
                                onPriceChange = { price ->
                                    viewModel.updateUnitPrice(cartItem.item.id, price)
                                },
                                onManualPriceEdited = { id -> viewModel.onManualPriceEdited(id) },
                                onRemove = { viewModel.removeFromCart(cartItem.item.id) }
                            )
                        }
                    }
                }

                // Total + Pay section on a soft mint panel
                Surface(
                    color = themeColors.mint,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                format.format(viewModel.subtotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(SpacingTokens.sm))
                        PayButton(
                            enabled = cartItems.isNotEmpty(),
                            payText = "Pay  ${format.format(viewModel.subtotal)}",
                            onClick = { viewModel.prepareCheckout() },
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = {
                showPaymentDialog = false
                viewModel.resetCheckoutState()
            },
            shape = ShapeTokens.dialog,
            title = {
                Text(
                    "Complete Payment",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                    selectedCustomer?.let {
                        Text("Customer: ${it.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "Total: ${format.format(viewModel.subtotal)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    when (val pmState = paymentMethods) {
                        is UiState.Loading -> {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(SpacingTokens.xxl))
                        }
                        is UiState.Error -> {
                            Text(pmState.message, color = MaterialTheme.colorScheme.error)
                        }
                        is UiState.Success -> {
                            Text(
                                "Payment Method",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            pmState.data.forEach { method ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = selectedPaymentMethod?.id == method.id,
                                        onClick = { viewModel.selectPaymentMethod(method) },
                                    )
                                    Text(
                                        "${method.name} (${method.methodType})",
                                        modifier = Modifier.padding(start = SpacingTokens.sm),
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    when (val cs = checkoutState) {
                        is UiState.Error -> Text(
                            cs.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        is UiState.Success ->
                            if (cs.data.status == OFFLINE_QUEUED_STATUS) {
                                Text(
                                    "No connection — sale saved on this device and will sync automatically when back online.",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                AppPrimaryButton(
                    text = if (checkoutState is UiState.Loading) "Processing..." else "Complete Sale",
                    onClick = { viewModel.checkout() },
                    enabled = selectedPaymentMethod != null && checkoutState !is UiState.Loading,
                )
            },
            dismissButton = {
                AppTextButton(
                    text = "Cancel",
                    onClick = {
                        showPaymentDialog = false
                        viewModel.resetCheckoutState()
                    },
                )
            },
        )
    }

    var showCustomerQuickAdd by remember { mutableStateOf(false) }
    if (showCustomerSearch) {
        CustomerSearchDialog(
            viewModel = viewModel,
            onDismiss = { showCustomerSearch = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerSearch = false
            },
            onAddNew = {
                showCustomerSearch = false
                showCustomerQuickAdd = true
            },
        )
    }

    if (showCustomerQuickAdd) {
        CustomerQuickAddDialog(
            viewModel = viewModel,
            onDismiss = { showCustomerQuickAdd = false },
        )
    }

    if (showNoStockDialog) {
        NoStockBalanceDialog(
            items = (stockGate as? StockGate.Missing)?.items.orEmpty(),
            adjustingItemId = adjustingItemId,
            error = adjustError,
            onAdjust = { itemId, qty -> viewModel.adjustStockFor(itemId, qty) },
            onDismiss = {
                showNoStockDialog = false
                viewModel.consumeStockGate()
            },
        )
    }
}

/** Full-width brand-green pay button with the card icon. */
@Composable
private fun PayButton(
    enabled: Boolean,
    payText: String,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val dark = AppThemeColors.current.greenDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(primary, dark)),
                shape = ShapeTokens.button,
                alpha = if (enabled) 1f else 0.45f,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = payText },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_payment_card),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(SpacingTokens.sm))
        Text(
            text = payText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

/** Rounded price-list chip: solid green when selected, soft outline otherwise. */
@Composable
private fun PriceListChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val themeColors = AppThemeColors.current
    val bg = if (selected) MaterialTheme.colorScheme.primary else themeColors.greenSoft
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(bg, ShapeTokens.chip)
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs)
            .semantics { contentDescription = if (selected) "$label selected" else label },
    )
}

/**
 * Header chip for offline sales: shows a cloud-off badge with the number of
 * queued sales (tap = sync now), a spinning sync icon while syncing, and
 * nothing when fully synced.
 */
@Composable
private fun OfflineSyncBadge(
    pendingCount: Int,
    isSyncing: Boolean,
    onClick: () -> Unit,
) {
    if (isSyncing) {
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.2f), ShapeTokens.chip)
                .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(SpacingTokens.xs))
            Text(
                "Syncing",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        return
    }
    if (pendingCount <= 0) return
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.22f), ShapeTokens.chip)
            .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs)
            .semantics { contentDescription = "Sync $pendingCount offline sales" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(SpacingTokens.xxs))
        Text(
            "$pendingCount offline",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun NoStockBalanceDialog(
    items: List<CartItem>,
    adjustingItemId: String?,
    error: String?,
    onAdjust: (String, BigDecimal) -> Unit,
    onDismiss: () -> Unit,
) {
    val qtyByItem = remember { mutableStateMapOf<String, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeTokens.dialog,
        title = { Text("No stock balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                Text(
                    "These items have no stock at your location. Adjust the stock before paying.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                items.forEach { cartItem ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            cartItem.item.name,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedTextField(
                            value = qtyByItem[cartItem.item.id] ?: "",
                            onValueChange = { qtyByItem[cartItem.item.id] = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Adjustment quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = adjustingItemId == null,
                        )
                    }
                }
                if (adjustingItemId != null) {
                    Text(
                        "Adjusting…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Apply adjustment",
                onClick = {
                    items.forEach { cartItem ->
                        val qty = qtyByItem[cartItem.item.id]?.toBigDecimalOrNull()
                        if (qty != null && qty.compareTo(BigDecimal.ZERO) != 0) {
                            onAdjust(cartItem.item.id, qty)
                        }
                    }
                },
                enabled = adjustingItemId == null,
            )
        },
        dismissButton = {
            AppTextButton(text = "Cancel", onClick = onDismiss)
        },
    )
}

@Composable
private fun ProductSearchItem(item: ItemDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = buildList {
                item.code?.let { add(it) }
                item.saleUom?.name?.let { add("UOM: $it") }
            }.joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppThemeColors.current.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onQuantityChange: (BigDecimal) -> Unit,
    onPriceChange: (BigDecimal) -> Unit,
    onManualPriceEdited: (String) -> Unit,
    onRemove: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    val themeColors = AppThemeColors.current
    var priceText by remember(item.unitPrice) { mutableStateOf(item.unitPrice.toPlainString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.md,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.card),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(SpacingTokens.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.item.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.uomName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppThemeColors.current.muted,
                        )
                    }
                }
                AppIconButton(
                    icon = Icons.Default.Close,
                    onClick = onRemove,
                    description = "Remove ${item.item.name}",
                )
            }
            // Stepper row: [−] [price input] [+]   <list name>   line total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityButton(
                    icon = Icons.Default.Remove,
                    description = "Decrease quantity",
                    onClick = { onQuantityChange(item.quantity.subtract(BigDecimal.ONE)) },
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        priceText = input
                        input.toBigDecimalOrNull()?.let {
                            onManualPriceEdited(item.item.id)
                            onPriceChange(it)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = SpacingTokens.xs),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Unit price", style = MaterialTheme.typography.labelSmall) },
                )
                QuantityButton(
                    icon = Icons.Default.Add,
                    description = "Increase quantity",
                    onClick = { onQuantityChange(item.quantity.add(BigDecimal.ONE)) },
                )
                Spacer(modifier = Modifier.width(SpacingTokens.sm))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        format.format(item.lineTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = item.priceSource?.let { "$it · ×${item.quantity.toPlainString()}" }
                            ?: "×${item.quantity.toPlainString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppThemeColors.current.muted,
                    )
                }
            }
        }
    }
}

/** Round green quantity button (− / +). */
@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CustomerSearchDialog(
    viewModel: PosTerminalViewModel,
    onDismiss: () -> Unit,
    onSelect: (PartyDto) -> Unit,
    onAddNew: () -> Unit,
) {
    val customerResults by viewModel.customerSearchResults.collectAsState()
    var search by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeTokens.dialog,
        title = { Text("Select Customer", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                AppSearchBar(
                    query = search,
                    onQueryChange = {
                        search = it
                        if (it.length >= 2) {
                            viewModel.searchCustomers(it)
                        }
                    },
                    placeholder = "Search customers...",
                    searchDescription = "Search customers",
                    clearDescription = "Clear search",
                )

                when (val state = customerResults) {
                    is UiState.Loading -> {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(SpacingTokens.xxl))
                    }
                    is UiState.Success -> {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            item {
                                ListItem(
                                    headlineContent = { Text("Walk-in Customer") },
                                    leadingContent = {
                                        Icon(Icons.Default.Person, contentDescription = "Walk-in")
                                    },
                                    modifier = Modifier
                                        .clickable {
                                            onSelect(PartyDto(id = "", name = "Walk-in Customer"))
                                        }
                                        .semantics { contentDescription = "Walk-in Customer" },
                                )
                            }
                            items(state.data, key = { it.id }) { customer ->
                                ListItem(
                                    headlineContent = { Text(customer.name) },
                                    supportingContent = customer.phone?.let { { Text(it) } },
                                    modifier = Modifier
                                        .clickable { onSelect(customer) }
                                        .semantics { contentDescription = customer.name },
                                )
                            }
                        }
                    }
                    is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                    else -> {}
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                AppTextButton(text = "Add New", onClick = onAddNew)
                AppTextButton(text = "Close", onClick = onDismiss)
            }
        },
    )
}

/**
 * Quick walk-in customer entry from the POS customer picker: phone number
 * (required, used to find the customer later) + optional name. When the name
 * is left blank the customer is created as "<Weekday>@<HH:mm>" (e.g.
 * "Monday@14:05"). The created customer is selected for the current sale.
 */
@Composable
private fun CustomerQuickAddDialog(
    viewModel: PosTerminalViewModel,
    onDismiss: () -> Unit,
) {
    val createState by viewModel.customerCreateState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val saving = createState is CustomerQuickAddState.Saving

    // Dismiss once the customer is created (the VM already selects it).
    LaunchedEffect(createState) {
        if (createState is CustomerQuickAddState.Created) onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeTokens.dialog,
        title = { Text("New Customer", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(15) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name (optional)") },
                    supportingText = {
                        Text(
                            "Leave empty to use ${viewModel.generateAutoCustomerName()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    singleLine = true,
                )

                when (val st = createState) {
                    is CustomerQuickAddState.DuplicateFound -> {
                        Text(
                            "A customer with this phone already exists:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        ListItem(
                            headlineContent = { Text(st.existing.name) },
                            supportingContent = st.existing.phone?.let { { Text(it) } },
                            leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier
                                .clickable {
                                    viewModel.useExistingCustomer(st.existing)
                                    onDismiss()
                                }
                                .semantics { contentDescription = "Use existing customer" },
                        )
                        Text(
                            "Tap the customer above to use it, or create a new one anyway.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    is CustomerQuickAddState.Failure -> Text(
                        st.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    else -> {}
                }
            }
        },
        confirmButton = {
            // First press runs the duplicate check; if a duplicate was
            // offered, this button becomes the explicit "create anyway".
            val duplicateOffered = createState is CustomerQuickAddState.DuplicateFound
            AppPrimaryButton(
                text = when {
                    saving -> "Saving..."
                    duplicateOffered -> "Create Anyway"
                    else -> "Save"
                },
                onClick = {
                    viewModel.createCustomer(
                        name = name,
                        phone = phone,
                        force = duplicateOffered,
                    )
                },
                enabled = phone.isNotBlank() && !saving,
            )
        },
        dismissButton = {
            AppTextButton(text = "Cancel", onClick = onDismiss)
        },
    )
}
