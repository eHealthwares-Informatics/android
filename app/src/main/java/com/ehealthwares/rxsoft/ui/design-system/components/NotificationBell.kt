package com.ehealthwares.rxsoft.ui.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        badge = {
            if (unreadCount > 0) {
                AppBadge(count = unreadCount)
            } else {
                Badge()
            }
        },
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.semantics {
                contentDescription = if (unreadCount > 0) {
                    "Notifications, $unreadCount unread"
                } else {
                    "Notifications"
                }
            },
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
