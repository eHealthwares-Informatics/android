package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        Snackbar(
            snackbarData = data,
            shape = ShapeTokens.md,
            actionOnNewLine = false,
        )
    }
}

suspend fun SnackbarHostState.showInfo(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    if (actionLabel != null && onAction != null) {
        this.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
        )
    } else {
        this.showSnackbar(
            message = message,
            duration = duration,
        )
    }
}
