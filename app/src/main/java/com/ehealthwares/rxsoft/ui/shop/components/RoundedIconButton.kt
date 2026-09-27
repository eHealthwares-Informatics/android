package com.rxsoft.mobile.ui.shop.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

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
        tonalElevation = ElevationTokens.sm,
        shadowElevation = ElevationTokens.xxs,
    ) {
        Box(
            modifier = Modifier
                .size(SpacingTokens.xxxxl)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
        }
    }
}
