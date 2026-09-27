package com.rxsoft.mobile.ui.shop.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun QuantitySelector(
    quantity: Int,
    modifier: Modifier = Modifier,
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = ShapeTokens.md,
        tonalElevation = ElevationTokens.xxs,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onDecrease,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease")
            }
            Spacer(Modifier.width(SpacingTokens.md))
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(SpacingTokens.md))
            FilledIconButton(
                onClick = onIncrease,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase")
            }
        }
    }
}
