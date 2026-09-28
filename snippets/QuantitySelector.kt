package com.ehealthwares.rxsoft.ui.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    quantity: Int,
    modifier: Modifier = Modifier,
    onIncrease: () -> Unit = {},
    onDecrease: () -> Unit = {}
) {

    Surface(
        modifier = modifier,
        shape = CircleShape,
        tonalElevation = 2.dp
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {

            Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Decrease",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDecrease() }
                    .padding(4.dp)
            )

            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(24.dp)
            )

            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Increase",
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onIncrease() }
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}