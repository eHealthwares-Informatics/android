package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun AppLoadingState(
    modifier: Modifier = Modifier,
    description: String = "Loading",
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun AppLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    description: String = "Loading",
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AppLinearProgress(
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(modifier = modifier.fillMaxWidth())
}

@Composable
fun AppSkeletonLoader(
    lineCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        repeat(lineCount) { index ->
            val widthFraction = when (index % 3) {
                0 -> 0.9f
                1 -> 0.75f
                else -> 0.6f
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(SpacingTokens.lg)
                    .clip(RoundedCornerShape(SpacingTokens.xs))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { contentDescription = "Loading placeholder" },
            )
        }
    }
}

@Composable
fun AppPageLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SkeletonHeader()
        repeat(3) { SkeletonListItem() }
    }
}

@Composable
private fun SkeletonHeader() {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(SpacingTokens.xs))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = "Loading" },
    )
}

@Composable
private fun SkeletonListItem() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(SpacingTokens.sm))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(SpacingTokens.xxs))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.height(SpacingTokens.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(SpacingTokens.xxs))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}
