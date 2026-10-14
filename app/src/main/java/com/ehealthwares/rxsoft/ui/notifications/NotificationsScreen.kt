package com.ehealthwares.rxsoft.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.remote.dto.NotificationItem
import com.ehealthwares.rxsoft.ui.designsystem.components.AppEmptyState
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val SOURCE_ENTITY_TYPE_REQUEST = "request"

@Composable
fun NotificationsScreen(
    onMenuClick: (() -> Unit)? = null,
    onRequestClick: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AppTopAppBar(
                title = "Notifications",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllRead() },
                        enabled = unreadCount > 0,
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                    }
                },
            )

            if (notifications.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Notifications,
                    title = "No notifications",
                    subtitle = "You're all caught up",
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = SpacingTokens.screenHorizontal,
                        end = SpacingTokens.screenHorizontal,
                        top = SpacingTokens.sm,
                        bottom = SpacingTokens.lg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = {
                                viewModel.markRead(notification.id)
                                if (notification.sourceEntityType == SOURCE_ENTITY_TYPE_REQUEST) {
                                    notification.sourceEntityId?.let(onRequestClick)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (notification.read) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                },
            )
            .clickable(onClick = onClick)
            .padding(SpacingTokens.md),
        verticalAlignment = Alignment.Top,
    ) {
        if (!notification.read) {
            Box(
                modifier = Modifier
                    .padding(top = SpacingTokens.xs)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Spacer(Modifier.width(SpacingTokens.sm))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notification.title ?: "Notification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                notification.sourceEntityRef?.let { ref ->
                    Spacer(Modifier.width(SpacingTokens.xs))
                    Text(
                        text = "#$ref",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            notification.body?.let { body ->
                Spacer(Modifier.height(SpacingTokens.xxs))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(SpacingTokens.xxs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                notification.type?.let { type ->
                    Text(
                        text = type.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatNotificationTime(createdAt: String): String {
    if (createdAt.isBlank()) return ""
    return runCatching {
        val instant = Instant.parse(createdAt)
        DateTimeFormatter.ofPattern("MMM d, h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrDefault(createdAt.take(10))
}
