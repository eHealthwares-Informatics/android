package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
) {
    if (count <= 0) return
    val displayText = if (count > maxCount) "$maxCount+" else count.toString()
    val isSingleDigit = count < 10

    Box(
        modifier = modifier
            .then(
                if (isSingleDigit) Modifier.size(18.dp)
                else Modifier.widthIn(min = 18.dp)
            )
            .background(MaterialTheme.colorScheme.error, CircleShape)
            .padding(horizontal = if (isSingleDigit) 0.dp else 4.dp, vertical = 2.dp)
            .semantics { contentDescription = "$count notifications" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = displayText,
            color = MaterialTheme.colorScheme.onError,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
        )
    }
}
