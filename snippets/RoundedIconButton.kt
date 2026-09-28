package com.ehealthwares.rxsoft.ui.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun RoundedIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {

    Surface(
        modifier = modifier,
        shape = CircleShape,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )

        }
    }

}