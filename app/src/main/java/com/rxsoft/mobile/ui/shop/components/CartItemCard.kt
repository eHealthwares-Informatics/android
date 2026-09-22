package com.rxsoft.mobile.ui.shop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.ui.shop.model.CartItem

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
        shape = ShapeTokens.xxl,
        elevation = CardDefaults.cardElevation(ElevationTokens.card),
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(SpacingTokens.lg))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name.ifBlank { "Product Name" },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(SpacingTokens.sm))
                Text(
                    text = item.product.unit.ifBlank { "Each" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(SpacingTokens.md))
                QuantitySelector(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }
            Spacer(Modifier.width(SpacingTokens.sm))
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(SpacingTokens.md))
                Text(
                    text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "NG")).format(item.totalPrice),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
