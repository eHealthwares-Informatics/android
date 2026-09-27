package com.rxsoft.mobile.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.ui.shop.components.PrimaryButton
import com.rxsoft.mobile.ui.shop.components.ProductImage
import com.rxsoft.mobile.ui.shop.components.QuantitySelector
import com.rxsoft.mobile.ui.shop.components.RoundedIconButton
import com.rxsoft.mobile.ui.shop.model.Product
import com.rxsoft.mobile.util.UiState

@Composable
fun ProductDetailScreen(
    itemId: String,
    onBack: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val productState by viewModel.productState.collectAsState()
    val quantity by viewModel.quantity.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    val product = (productState as? UiState.Success)?.data

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "",
                onBack = null,
                actions = {
                    RoundedIconButton(icon = Icons.Outlined.ArrowBack, onClick = onBack)
                    RoundedIconButton(icon = Icons.Outlined.FavoriteBorder, onClick = { })
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = ElevationTokens.xxl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.xl),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg)
                ) {
                    QuantitySelector(
                        quantity = quantity,
                        modifier = Modifier.weight(.8f),
                        onIncrease = { viewModel.increaseQuantity() },
                        onDecrease = { viewModel.decreaseQuantity() }
                    )
                    PrimaryButton(
                        text = "Buy Now",
                        modifier = Modifier.weight(1.2f),
                        onClick = {
                            product?.let { viewModel.addToCart(it, quantity) }
                            onAddToCart()
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (productState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((productState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = SpacingTokens.xl)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(SpacingTokens.md))
                    ProductImage(imageUrl = product?.imageUrl)
                    Spacer(Modifier.height(SpacingTokens.xxl))
                    Text(
                        text = product?.name ?: "Product Name",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(SpacingTokens.sm))
                    Text(
                        text = product?.unit ?: "30X",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(SpacingTokens.xxl))
                    Text("Description", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(SpacingTokens.sm))
                    Text(
                        text = if (product?.description.isNullOrBlank()) "Product description goes here."
                        else product!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(SpacingTokens.sm))
                    AppTextButton(text = "Read More", onClick = { })
                    Spacer(Modifier.height(SpacingTokens.xxl))
                    Text(
                        text = java.text.NumberFormat
                            .getCurrencyInstance(java.util.Locale("en", "NG"))
                            .format(product?.price ?: 0.0),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(100.dp))
                }
            }
            is UiState.Idle -> Unit
        }
    }
}
