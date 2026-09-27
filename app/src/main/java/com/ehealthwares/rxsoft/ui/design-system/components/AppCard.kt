package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun AppCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = modifier.semantics {
        this.contentDescription = contentDescription ?: "Card"
    }
    val shape = ShapeTokens.card
    val elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.card)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            elevation = elevation,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) { content() }
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            elevation = elevation,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) { content() }
        }
    }
}

@Composable
fun AppOutlinedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = modifier.semantics {
        this.contentDescription = contentDescription ?: "Card"
    }
    val shape = ShapeTokens.card
    val colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)

    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) { content() }
        }
    } else {
        OutlinedCard(
            modifier = cardModifier,
            shape = shape,
            colors = colors,
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.cardPadding)) { content() }
        }
    }
}
