package com.ehealthwares.rxsoft.ui.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.ui.shop.components.*
import com.ehealthwares.rxsoft.ui.shop.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product = Product(),
    quantity: Int = 1,
    onBack: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onBuy: () -> Unit = {}
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {},

                navigationIcon = {

                    RoundedIconButton(
                        icon = Icons.Outlined.ArrowBack,
                        onClick = onBack
                    )

                },

                actions = {

                    RoundedIconButton(
                        icon = Icons.Outlined.FavoriteBorder,
                        onClick = onFavorite
                    )

                }

            )

        },

        bottomBar = {

            Surface(
                shadowElevation = 8.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    QuantitySelector(
                        quantity = quantity,
                        modifier = Modifier.weight(.8f),
                        onIncrease = onIncrease,
                        onDecrease = onDecrease
                    )

                    PrimaryButton(
                        text = "Buy Now",
                        modifier = Modifier.weight(1.2f),
                        onClick = onBuy
                    )

                }

            }

        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))

            ProductImage()

            Spacer(Modifier.height(24.dp))

            Text(
                text = product.name.ifBlank { "Product Name" },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = product.unit.ifBlank { "30X" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Description",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text =
                if (product.description.isBlank())
                    "Product description goes here."
                else
                    product.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = {}
            ) {

                Text("Read More")

            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "$0.00",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(100.dp))

        }

    }

}