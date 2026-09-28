package com.ehealthwares.rxsoft.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.ui.shop.components.CartItemCard
import com.ehealthwares.rxsoft.ui.shop.components.PaymentSummary
import com.ehealthwares.rxsoft.ui.shop.components.PrimaryButton
import com.ehealthwares.rxsoft.ui.shop.components.RoundedIconButton
import com.ehealthwares.rxsoft.ui.shop.components.VoucherCard
import com.ehealthwares.rxsoft.ui.shop.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem> = emptyList(),
    subtotal: String = "$0.00",
    deliveryFee: String = "$0.00",
    total: String = "$0.00",
    onBack: () -> Unit = {},
    onAddProduct: () -> Unit = {},
    onIncreaseQuantity: (CartItem) -> Unit = {},
    onDecreaseQuantity: (CartItem) -> Unit = {},
    onDeleteItem: (CartItem) -> Unit = {},
    onVoucherClick: () -> Unit = {},
    onPayNow: () -> Unit = {}
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Checkout")
                },

                navigationIcon = {

                    RoundedIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        onClick = onBack
                    )

                }

            )

        },

        bottomBar = {

            Surface(
                shadowElevation = 12.dp
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    PrimaryButton(
                        text = "Pay Now",
                        onClick = onPayNow
                    )

                }

            }

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(20.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)

        ) {

            if (cartItems.isEmpty()) {

                item {

                    EmptyCartCard(
                        onAddProduct = onAddProduct
                    )

                }

            } else {

                items(cartItems) { item ->

                    CartItemCard(

                        item = item,

                        onIncrease = {
                            onIncreaseQuantity(item)
                        },

                        onDecrease = {
                            onDecreaseQuantity(item)
                        },

                        onDelete = {
                            onDeleteItem(item)
                        }

                    )

                }

            }

            item {

                AddProductCard(
                    onClick = onAddProduct
                )

            }

            item {

                VoucherCard(
                    onClick = onVoucherClick
                )

            }

            item {

                PaymentSummary(
                    subtotal = subtotal,
                    delivery = deliveryFee,
                    total = total
                )

            }

            item {

                Spacer(
                    modifier = Modifier.height(80.dp)
                )

            }

        }

    }

}

@Composable
private fun AddProductCard(
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {

        Row(

            modifier = Modifier.padding(20.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            Box(

                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),

                contentAlignment = Alignment.Center

            ) {

                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )

            }

            Column {

                Text(
                    text = "Add Product",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Add another product to your cart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

        }

    }

}

@Composable
private fun EmptyCartCard(
    onAddProduct: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(

            modifier = Modifier.padding(32.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(
                text = "Your cart is empty",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Add products to begin checkout.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onAddProduct
            ) {

                Text("Add Product")

            }

        }

    }

}