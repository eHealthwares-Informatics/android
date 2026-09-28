package com.ehealthwares.rxsoft.ui.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.ui.shop.model.CartItem

@Composable
fun CartItemCard(
    item: CartItem,
    modifier: Modifier = Modifier,
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onDelete: () -> Unit = {}
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = item.product.name.ifBlank { "Product Name" },
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (item.product.unit.isBlank()) "30X" else item.product.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                QuantitySelector(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )

            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "$0.00",
                    style = MaterialTheme.typography.titleMedium
                )

            }

        }

    }

}