package com.rxsoft.mobile.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@Composable
fun EnterPinScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    resetTrigger: Int = 0,
    viewModel: EnterPinViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.colorScheme
    val mode by viewModel.mode.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val error by viewModel.error.collectAsState()
    val remainingAttempts by viewModel.remainingAttempts.collectAsState()
    val shakeTrigger by viewModel.shakeTrigger.collectAsState()
    val navigation by viewModel.navigation.collectAsState()
    val isVerifying by viewModel.isVerifying.collectAsState()

    LaunchedEffect(resetTrigger) {
        if (resetTrigger > 0) {
            viewModel.resetState()
        }
    }

    // Always start with a blank pad when the PIN screen is shown.
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    var showForgotPinDialog by remember { mutableStateOf(false) }

    if (showForgotPinDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = { Text("Forgot PIN", color = colors.onSurface) },
            text = { Text("You'll need to sign in again to reset your PIN.", color = colors.onSurfaceVariant) },
            containerColor = colors.surface,
            confirmButton = {
                TextButton(onClick = {
                    showForgotPinDialog = false
                    // Wipe the in-memory PIN state before leaving so a fresh
                    // session starts from a blank PIN pad.
                    viewModel.resetState()
                    viewModel.onForgotPin()
                }) {
                    Text("Sign In Again", color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPinDialog = false }) {
                    Text("Cancel", color = colors.onSurfaceVariant)
                }
            },
        )
    }

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            for (i in 1..3) {
                shakeOffset.animateTo(12f, tween(40))
                shakeOffset.animateTo(-12f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    LaunchedEffect(navigation) {
        when (navigation) {
            PinNavigationEvent.NavigateToHome -> {
                viewModel.clearNavigation()
                onNavigateToHome()
            }
            PinNavigationEvent.NavigateToLogin -> {
                viewModel.clearNavigation()
                onNavigateToLogin()
            }
            null -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, shakeOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(64.dp))

            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Fingerprint",
                tint = colors.primary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = viewModel.title,
                color = colors.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = viewModel.subtitle,
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp),
            )

            Spacer(Modifier.height(32.dp))

            PinDots(
                pin = pin,
                length = 4,
                error = error != null,
            )

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error!!,
                    color = colors.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }

            // Durable attempts countdown: stays visible (even after the user
            // starts typing again) until the PIN is verified successfully.
            if (remainingAttempts != null && mode is PinMode.Unlock) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$remainingAttempts attempt${if (remainingAttempts == 1) "" else "s"} remaining",
                    color = if ((remainingAttempts ?: 5) <= 2) colors.error else colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(40.dp))

            PinKeypad(
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDelete,
                enabled = !isVerifying,
            )

            Spacer(Modifier.weight(1f))

            PinBottomActions(
                mode = mode,
                onCancel = viewModel::onCancel,
                onForgotPin = { showForgotPinDialog = true },
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PinDots(pin: String, length: Int, error: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(length) { index ->
            val isFilled = index < pin.length
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (error) colors.error
                        else if (isFilled) colors.primary
                        else colors.outline
                    )
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            KeyButton("1", onDigit, enabled)
            KeyButton("2", onDigit, enabled)
            KeyButton("3", onDigit, enabled)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            KeyButton("4", onDigit, enabled)
            KeyButton("5", onDigit, enabled)
            KeyButton("6", onDigit, enabled)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            KeyButton("7", onDigit, enabled)
            KeyButton("8", onDigit, enabled)
            KeyButton("9", onDigit, enabled)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.size(76.dp))
            KeyButton("0", onDigit, enabled)
            DeleteButton(onDelete, enabled)
        }
    }
}

@Composable
private fun KeyButton(
    digit: String,
    onDigit: (String) -> Unit,
    enabled: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true),
                enabled = enabled,
            ) { onDigit(digit) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit,
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun DeleteButton(
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled) { onDelete() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Delete",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PinBottomActions(
    mode: PinMode,
    onCancel: () -> Unit,
    onForgotPin: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text(
                text = "Cancel",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
            )
        }

        if (mode is PinMode.Unlock) {
            TextButton(onClick = onForgotPin) {
                Text(
                    text = "Forgot PIN",
                    color = colors.primary,
                    fontSize = 14.sp,
                )
            }
        } else {
            TextButton(onClick = { }) {
                Text(
                    text = "",
                    fontSize = 14.sp,
                )
            }
        }
    }
}
