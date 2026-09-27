package com.rxsoft.mobile.ui.shop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.ui.designsystem.components.AppFilterChip
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.ui.shop.components.CartItemCard
import com.rxsoft.mobile.ui.shop.components.PaymentSummary
import com.rxsoft.mobile.ui.shop.components.PrimaryButton
import com.rxsoft.mobile.ui.shop.components.VoucherCard
import com.rxsoft.mobile.util.UiState
import com.rxsoft.mobile.util.payment.PaymentProviderType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CheckoutScreen(
    onBack: () -> Unit = {},
    onAddProduct: () -> Unit = {},
    onOrderCreated: (String) -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    var pendingReference by remember { mutableStateOf<String?>(null) }
    var pendingProvider by remember { mutableStateOf<PaymentProviderType?>(null) }

    val checkoutLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val reference = pendingReference
        val provider = pendingProvider
        if (reference != null && provider != null) {
            viewModel.onCheckoutReturned(reference, provider)
        }
        pendingReference = null
        pendingProvider = null
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentEvent.LaunchCheckout -> {
                    pendingReference = event.reference
                    pendingProvider = event.provider
                    val intent = viewModel.buildCheckoutIntent(event.provider, event.url)
                    checkoutLauncher.launch(intent)
                }
            }
        }
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is UiState.Success) {
            val sale = (checkoutState as UiState.Success<*>).data
            if (sale is com.rxsoft.mobile.data.remote.dto.SaleDto) {
                onOrderCreated(sale.id)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Checkout",
                onBack = onBack,
            )
        },
        bottomBar = {
            Surface(shadowElevation = ElevationTokens.xxxl) {
                Column(modifier = Modifier.padding(SpacingTokens.xl)) {
                    if (checkoutState is UiState.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(SpacingTokens.sm))
                    }
                    PrimaryButton(
                        text = "Pay Now",
                        enabled = cartItems.isNotEmpty() && checkoutState !is UiState.Loading,
                        onClick = { viewModel.pay() }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(SpacingTokens.xl),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
        ) {
            if (cartItems.isEmpty()) {
                item { EmptyCartCard(onAddProduct = onAddProduct) }
            } else {
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.updateQuantity(item.product.id, item.quantity + 1) },
                        onDecrease = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                        onDelete = { viewModel.removeItem(item.product.id) }
                    )
                }
            }

            item {
                PaymentMethodCard(
                    providers = viewModel.providers,
                    selected = selectedProvider,
                    isInstalled = { viewModel.isProviderAppInstalled(it) },
                    onSelect = { viewModel.selectProvider(it) },
                )
            }
            item { AddProductCard(onClick = onAddProduct) }
            item { VoucherCard(onClick = { }) }
            item {
                PaymentSummary(
                    subtotal = format.format(viewModel.subtotal),
                    delivery = format.format(0.0),
                    total = format.format(viewModel.subtotal)
                )
            }
            if (checkoutState is UiState.Error) {
                item {
                    Text(
                        (checkoutState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    providers: List<PaymentProviderType>,
    selected: PaymentProviderType,
    isInstalled: (PaymentProviderType) -> Boolean,
    onSelect: (PaymentProviderType) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.xl)) {
            Text("Payment method", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(SpacingTokens.md))
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                providers.forEach { provider ->
                    AppFilterChip(
                        selected = selected == provider,
                        onClick = { onSelect(provider) },
                        label = provider.displayName,
                    )
                }
            }
            if (selected.usesGateway) {
                Spacer(Modifier.height(SpacingTokens.sm))
                Text(
                    text = when {
                        !selected.supportsOnlineCheckout ->
                            "${selected.displayName} requires a configured POS terminal."
                        isInstalled(selected) ->
                            "${selected.displayName} app detected — paying in the app."
                        else ->
                            "Paying with ${selected.displayName} via secure checkout."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddProductCard(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(SpacingTokens.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add product")
            }
            Column {
                Text("Add Product", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Add another product to your cart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyCartCard(onAddProduct: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(SpacingTokens.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Your cart is empty", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(SpacingTokens.md))
            Text(
                "Add products to begin checkout.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(SpacingTokens.xxl))
            com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton(
                text = "Add Product",
                onClick = onAddProduct,
            )
        }
    }
}
