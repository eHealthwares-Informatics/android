package com.ehealthwares.rxsoft.data.repository

import android.util.Log
import com.ehealthwares.rxsoft.data.remote.api.NotificationApi
import com.ehealthwares.rxsoft.data.remote.dto.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val notificationApi: NotificationApi,
) {
    companion object {
        private const val TAG = "NotificationsRepository"
        private const val POLL_INTERVAL_MS = 30_000L
        private const val PAGE_SIZE = 50
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var pollJob: Job? = null
    private var lastCursor: String? = null

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive) {
                refresh()
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    suspend fun refresh() {
        fetchUnreadCount()
        fetchNotifications()
    }

    private suspend fun fetchNotifications() {
        runCatching { notificationApi.getNotifications(since = lastCursor, limit = PAGE_SIZE) }
            .onSuccess { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    mergeNotifications(body.data)
                    lastCursor = body.data.maxOfOrNull { it.createdAt }
                    _error.value = null
                }
            }
            .onFailure { e ->
                Log.w(TAG, "Failed to fetch notifications: ${e.message}")
                _error.value = e.message
            }
    }

    private suspend fun fetchUnreadCount() {
        runCatching { notificationApi.getUnreadCount() }
            .onSuccess { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _unreadCount.value = body.count
                }
            }
            .onFailure { e ->
                Log.w(TAG, "Failed to fetch unread count: ${e.message}")
            }
    }

    private fun mergeNotifications(incoming: List<NotificationItem>) {
        val existing = _notifications.value.associateBy { it.id }
        val merged = incoming.map { item ->
            existing[item.id]?.takeIf { it.read } ?: item
        }
        val mergedMap = merged.associateBy { it.id }
        val result = (_notifications.value.mapNotNull { mergedMap[it.id] } + merged)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
        _notifications.value = result
    }

    suspend fun markRead(id: String) {
        runCatching { notificationApi.markRead(id) }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    _notifications.value = _notifications.value.map {
                        if (it.id == id) it.copy(read = true) else it
                    }
                    if (_unreadCount.value > 0) _unreadCount.value -= 1
                }
            }
            .onFailure { e ->
                Log.w(TAG, "Failed to mark notification $id as read: ${e.message}")
            }
    }

    suspend fun markAllRead() {
        runCatching { notificationApi.markAllRead() }
            .onSuccess { response ->
                if (response.isSuccessful) {
                    _notifications.value = _notifications.value.map { it.copy(read = true) }
                    _unreadCount.value = 0
                }
            }
            .onFailure { e ->
                Log.w(TAG, "Failed to mark all notifications as read: ${e.message}")
            }
    }

    suspend fun registerSubscription() {
        runCatching { notificationApi.registerSubscription(emptyMap()) }
            .onFailure { e ->
                if (e.message?.contains("404") != true) {
                    Log.w(TAG, "Failed to register subscription: ${e.message}")
                }
            }
    }
}
