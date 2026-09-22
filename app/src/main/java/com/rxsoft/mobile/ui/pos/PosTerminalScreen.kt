package com.rxsoft.mobile.ui.pos

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.remote.dto.PartyDto
import com.rxsoft.mobile.data.remote.dto.PaymentMethodDto
import com.rxsoft.mobile.ui.designsystem.components.AppFilterChip
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppSearchBar
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBarActions
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.ReceiptData
import com.rxsoft.mobile.util.ReceiptLine
import com.rxsoft.mobile.util.UiState
import com.rxsoft.mobile.util.printReceipt
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
            if (sale is com.rxsoft.mobile.data.remote.dto.SaleDto) {
                printReceipt(
                    context,
                    ReceiptData(
                        saleNumber = sale.saleNumber,
                        customerName = selectedCustomer?.name,
                        items = cartItems.map {
                            ReceiptLine(it.item.name, it.quantity, it.unitPrice, it.lineTotal)
                        },
                        subtotal = viewModel.subtotal,
                        total = viewModel.subtotal,
                        paidAmount = viewModel.subtotal,
                        changeAmount = BigDecimal.ZERO
                    )
                )
                onOrderCreated(sale.id)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "New Sale",
                onBack = onBack,
                actions = {
                    if (cartItems.isNotEmpty()) {
                        AppTopAppBarActions(
                            icon = Icons.Default.Delete,
                            onClick = { viewModel.clearCart() },
                            description = "Clear cart",
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
            )

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
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.screenHorizontal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = "Customer", modifier = Modifier.size(SpacingTokens.xl))
                Spacer(modifier = Modifier.width(SpacingTokens.sm))
                Text(
                    text = selectedCustomer?.name ?: "Walk-in Customer",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                AppTextButton(
                    text = if (selectedCustomer != null) "Change" else "Select",
                    onClick = { showCustomerSearch = true },
                )
            }

            viewModel.currentStockLocationName?.let { locName ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.screenHorizontal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", modifier = Modifier.size(SpacingTokens.xl))
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    Text(locName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (priceLists.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = SpacingTokens.screenHorizontal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Price list:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    priceLists.forEach { pl ->
                        AppFilterChip(
                            selected = selectedPriceListId == pl.id,
                            onClick = { viewModel.setPriceList(pl.id) },
                            label = pl.name.ifBlank { pl.code },
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                    }
                }
            }

            HorizontalDivider()

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Search and add products to start",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(SpacingTokens.sm),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
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
                            onRemove = { viewModel.removeFromCart(cartItem.item.id) }
                        )
                    }
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(
                        format.format(viewModel.subtotal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(SpacingTokens.sm))
                AppPrimaryButton(
                    text = "Pay - ${format.format(viewModel.subtotal)}",
                    onClick = {
                        viewModel.prepareCheckout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cartItems.isNotEmpty(),
                    leadingIcon = Icons.Default.ShoppingCart,
                    description = "Pay",
                )
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

                    if (checkoutState is UiState.Error) {
                        Text(
                            (checkoutState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
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

    if (showCustomerSearch) {
        CustomerSearchDialog(
            viewModel = viewModel,
            onDismiss = { showCustomerSearch = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerSearch = false
            }
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
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = buildList {
            item.code?.let { add(it) }
            item.saleUom?.name?.let { add("UOM: $it") }
        }.joinToString(" · ").let { { Text(it) } },
        trailingContent = {
            AppIconButton(
                icon = Icons.Default.Add,
                onClick = onClick,
                description = "Add ${item.name}",
            )
        },
    )
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onQuantityChange: (BigDecimal) -> Unit,
    onPriceChange: (BigDecimal) -> Unit,
    onRemove: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "NG"))
    var priceText by remember(item.unitPrice) { mutableStateOf(item.unitPrice.toPlainString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.md,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.card),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.item.name,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                )
                item.uomName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        priceText = input
                        input.toBigDecimalOrNull()?.let { onPriceChange(it) }
                    },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Price", style = MaterialTheme.typography.bodySmall) },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconButton(
                    icon = Icons.Default.Remove,
                    onClick = { onQuantityChange(item.quantity.subtract(BigDecimal.ONE)) },
                    description = "Decrease quantity",
                )
                Text(
                    item.quantity.toPlainString(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = SpacingTokens.xxs),
                )
                AppIconButton(
                    icon = Icons.Default.Add,
                    onClick = { onQuantityChange(item.quantity.add(BigDecimal.ONE)) },
                    description = "Increase quantity",
                )
            }
            Text(
                format.format(item.lineTotal),
                modifier = Modifier.padding(horizontal = SpacingTokens.sm),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            AppIconButton(
                icon = Icons.Default.Close,
                onClick = onRemove,
                description = "Remove ${item.item.name}",
            )
        }
    }
}

@Composable
private fun CustomerSearchDialog(
    viewModel: PosTerminalViewModel,
    onDismiss: () -> Unit,
    onSelect: (PartyDto) -> Unit
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
            AppTextButton(text = "Close", onClick = onDismiss)
        },
    )
}
