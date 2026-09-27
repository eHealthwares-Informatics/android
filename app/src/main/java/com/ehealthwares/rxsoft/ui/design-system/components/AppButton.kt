package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    description: String? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics {
                this.contentDescription = description ?: text
            },
        enabled = enabled,
        shape = ShapeTokens.button,
        contentPadding = PaddingValues(horizontal = SpacingTokens.xxl),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
            )
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { contentDescription = text },
        enabled = enabled,
        shape = ShapeTokens.button,
        contentPadding = PaddingValues(horizontal = SpacingTokens.xxl),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { contentDescription = text },
        enabled = enabled,
        shape = ShapeTokens.button,
        contentPadding = PaddingValues(horizontal = SpacingTokens.xxl),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = text },
        enabled = enabled,
        shape = ShapeTokens.button,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = description ?: "Icon button" },
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}
