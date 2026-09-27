package com.rxsoft.mobile.ui.chat

import android.util.Log
import com.rxsoft.mobile.BuildConfig
import com.rxsoft.mobile.data.local.CachedChatMessageEntity
import com.rxsoft.mobile.data.local.CachedConversationDao
import com.rxsoft.mobile.data.local.CachedChatMessageDao
import com.rxsoft.mobile.data.local.CachedConversationEntity
import com.rxsoft.mobile.data.local.ChatStateStore
import com.rxsoft.mobile.data.remote.api.ChatApi
import com.rxsoft.mobile.data.remote.dto.ConversationInboxItem
import com.rxsoft.mobile.data.remote.dto.ExchangeMessage
import com.rxsoft.mobile.data.remote.dto.SendWebhookDto
import com.rxsoft.mobile.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Emitted when the conversation engine ends a conversation (e.g. questionnaire completed). */
data class ConversationEnded(
    val conversationId: String,
    val status: String?,
)

/**
 * Data layer for the Conversation Engine: inbox + messages REST calls, outbound
 * send via /webhooks/web, and realtime message/ended updates via the socket.
 *
 * Conversations and messages are persisted in Room (`cached_conversations` /
 * `cached_chat_messages`) and small UI state in DataStore (`ChatStateStore`):
 * the inbox renders instantly from cache on cold start, unread counts and the
 * last-open thread survive app restarts.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val socket: ChatSocket,
    private val tokenManager: TokenManager,
    private val conversationDao: CachedConversationDao,
    private val messageDao: CachedChatMessageDao,
    private val chatStateStore: ChatStateStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _inbox = MutableStateFlow<List<ConversationInboxItem>>(emptyList())
    val inbox: StateFlow<List<ConversationInboxItem>> = _inbox.asStateFlow()

    private val _messagesByConversation = MutableStateFlow<Map<String, List<ExchangeMessage>>>(emptyMap())
    val messagesByConversation: StateFlow<Map<String, List<ExchangeMessage>>> = _messagesByConversation.asStateFlow()

    private val _inboxError = MutableStateFlow<String?>(null)
    val inboxError: StateFlow<String?> = _inboxError.asStateFlow()

    /** Every realtime message event — screens use it to follow a newly-started conversation. */
    private val _messageArrived = MutableSharedFlow<ExchangeMessage>(extraBufferCapacity = 16)
    val messageArrived: SharedFlow<ExchangeMessage> = _messageArrived.asSharedFlow()

    private val _conversationEnded = MutableSharedFlow<ConversationEnded>(extraBufferCapacity = 8)
    val conversationEnded: SharedFlow<ConversationEnded> = _conversationEnded.asSharedFlow()

    /** The thread the user last viewed (survives restarts). */
    val lastOpen = chatStateStore.lastOpen

    /**
     * Number of conversations holding unread messages (survives restarts).
     * Kept in sync with Room by publishUnreadBadge(); drives any badge UI.
     */
    val conversationsWithUnread = chatStateStore.conversationsWithUnread

    /** Conversation room the user currently has open (memory-only, not persisted). */
    @Volatile
    private var viewingConversationId: String? = null

    init {
        // Hydrate from Room immediately so the inbox and unread badge render
        // from cache before the first network refresh returns.
        scope.launch {
            runCatching {
                _inbox.value = conversationDao.all().map { it.toDto() }
                publishUnreadBadge()
            }.onFailure { e -> Log.w(TAG, "Cache hydration failed: ${e.message}") }
        }
        socket.connect(
            onMessage = { message -> handleSocketMessage(message) },
            onInboxUpdated = {
                scope.launch { refreshInbox(loadOnFailure = true) }
            },
            onConversationEnded = { raw -> handleConversationEnded(raw) },
        )
    }

    suspend fun refreshInbox(loadOnFailure: Boolean = false) {
        runCatching { chatApi.inbox(limit = 30, activeOnly = true) }
            .onSuccess { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    persistInbox(body.items)
                    _inboxError.value = null
                } else if (loadOnFailure) {
                    _inboxError.value = "Inbox fetch failed (${response.code()})"
                }
            }
            .onFailure { e ->
                Log.w(TAG, "Inbox fetch failed: ${e.message}")
                if (loadOnFailure) _inboxError.value = e.message ?: "Inbox fetch failed"
            }
    }

    /** Cache-first load: surface Room instantly, then refresh from the network. */
    suspend fun loadMessages(conversationId: String) {
        // 1. Instant cache render.
        val cached = runCatching { messageDao.forConversation(conversationId) }.getOrDefault(emptyList())
        if (cached.isNotEmpty()) {
            _messagesByConversation.value = _messagesByConversation.value +
                (conversationId to cached.map { it.toDto() })
        }
        // 2. Network refresh.
        runCatching { chatApi.exchanges(conversationId = conversationId, limit = 30) }
            .onSuccess { response ->
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    persistMessages(conversationId, body.items)
                }
            }
            .onFailure { e -> Log.w(TAG, "Message fetch failed: ${e.message}") }
    }

    suspend fun sendText(conversationId: String?, text: String) {
        val phone = tokenManager.userPhone.first()
        runCatching {
            chatApi.sendWebhook(
                SendWebhookDto(
                    channelId = BuildConfig.DEFAULT_WEB_CHANNEL_ID,
                    senderPhone = phone.orEmpty(),
                    text = text,
                    conversationId = conversationId,
                )
            )
        }.onFailure { e -> Log.w(TAG, "Send failed: ${e.message}") }
    }

    suspend fun markRead(conversationId: String) {
        runCatching { chatApi.markRead(conversationId) }
        runCatching {
            conversationDao.markRead(conversationId)
            reloadInboxFromRoom()
        }
    }

    /** Remember the thread the user is viewing (restored on next launch). */
    fun openConversation(conversationId: String, title: String? = null) {
        viewingConversationId = conversationId
        socket.openConversation(conversationId)
        scope.launch {
            runCatching { chatStateStore.saveLastOpen(conversationId, title) }
        }
    }

    /** Leave a conversation room. */
    fun closeConversation(conversationId: String) {
        if (viewingConversationId == conversationId) viewingConversationId = null
        socket.closeConversation(conversationId)
    }

    /** True when the persisted last-open state marks this conversation ended. */
    suspend fun isConversationEnded(conversationId: String): Boolean {
        val last = chatStateStore.lastOpenSnapshot() ?: return false
        return last.conversationId == conversationId && last.ended
    }

    /** Wipe all locally cached chat state (used on logout / account switch). */
    suspend fun clearLocalCache() {
        runCatching {
            messageDao.clear()
            conversationDao.clear()
            chatStateStore.clearLastOpen()
            chatStateStore.setConversationsWithUnread(0)
            _inbox.value = emptyList()
            _messagesByConversation.value = emptyMap()
        }
    }

    private suspend fun persistInbox(items: List<ConversationInboxItem>) {
        runCatching {
            val now = System.currentTimeMillis()
            val existing = conversationDao.all().associateBy { it.conversationId }
            conversationDao.upsertAll(
                items.map { item ->
                    val prev = existing[item.conversationId]
                    CachedConversationEntity(
                        conversationId = item.conversationId,
                        channelId = item.channelId,
                        title = item.title,
                        status = item.status,
                        lastMessageText = item.lastMessageText,
                        lastMessageDirection = item.lastMessageDirection,
                        lastMessageAt = item.lastMessageAt,
                        // Server unread wins, but keep unreadDelta accumulated
                        // from locally-observed realtime messages since sync.
                        unreadCount = item.unreadCount,
                        unreadDelta = prev?.unreadDelta ?: 0,
                        cachedAt = now,
                    )
                },
            )
            reloadInboxFromRoom()
        }.onFailure { e -> Log.w(TAG, "Inbox persist failed: ${e.message}") }
    }

    private suspend fun persistMessages(conversationId: String, items: List<ExchangeMessage>) {
        runCatching {
            messageDao.upsertAll(items.map { it.toEntity() })
            _messagesByConversation.value =
                _messagesByConversation.value + (conversationId to items.reversed())
        }.onFailure { e -> Log.w(TAG, "Message persist failed: ${e.message}") }
    }

    private suspend fun reloadInboxFromRoom() {
        val cached = runCatching { conversationDao.all() }.getOrDefault(emptyList())
        _inbox.value = cached.map { it.toDto() }
        publishUnreadBadge()
    }

    private suspend fun publishUnreadBadge() {
        val withUnread = runCatching { conversationDao.all() }
            .getOrDefault(emptyList())
            .count { it.unreadCount > 0 || it.unreadDelta > 0 }
        runCatching { chatStateStore.setConversationsWithUnread(withUnread) }
    }

    private fun handleSocketMessage(raw: JSONObject) {
        scope.launch {
            val convId = raw.optString("conversationId")
            val text = raw.optString("text")
            if (convId.isEmpty()) return@launch

            val message = ExchangeMessage(
                id = raw.optString("id"),
                conversationId = convId,
                senderId = raw.optString("senderId"),
                receiverId = raw.optString("receiverId").ifEmpty { null },
                direction = raw.optString("direction", "outbound"),
                text = text,
                createdAt = raw.optString("createdAt"),
                status = raw.optString("status").ifEmpty { null },
            )

            // Persist the realtime message so the transcript and unread state
            // survive an app restart. Rows without a server id are skipped —
            // the network refresh persists them with their real ids.
            if (message.id.isNotEmpty()) {
                runCatching { messageDao.upsertAll(listOf(message.toEntity())) }
            }

            val existing = _messagesByConversation.value[convId].orEmpty()
            val hasSame = existing.any {
                (it.id.isNotEmpty() && it.id == message.id) ||
                    (it.text == text && it.direction == message.direction)
            }
            if (!hasSame) {
                _messagesByConversation.value =
                    _messagesByConversation.value + (convId to (existing + message))
            }
            _messageArrived.tryEmit(message)

            // Unread accounting: only bump when the user is not viewing the
            // thread (an open thread is marked read via markRead()).
            val isViewing = viewingConversationId == convId
            if (!isViewing && !message.direction.equals("outbound", ignoreCase = true)) {
                runCatching { conversationDao.incrementUnread(convId) }
            }

            // Refresh inbox from the network (updates last message + status);
            // unreadDelta survives the refresh via persistInbox().
            refreshInbox(loadOnFailure = true)
        }
    }

    /**
     * The engine ended a conversation: drop its cached transcript so a stale
     * thread is never shown, notify open screens to reset, and refresh the
     * inbox (the next inbound message starts a brand-new conversation).
     */
    private fun handleConversationEnded(raw: JSONObject) {
        scope.launch {
            val convId = raw.optString("conversationId")
            if (convId.isEmpty()) return@launch
            runCatching {
                messageDao.deleteByConversationId(convId)
                _messagesByConversation.value = _messagesByConversation.value - convId
            }
            _conversationEnded.tryEmit(
                ConversationEnded(
                    conversationId = convId,
                    status = raw.optString("status").ifEmpty { null },
                ),
            )
            // If this was the thread the user last viewed, remember it ended so
            // the restored screen after a restart shows the ended banner.
            val lastOpen = chatStateStore.lastOpenSnapshot()
            if (lastOpen?.conversationId == convId) {
                chatStateStore.saveLastOpenEnded(convId)
            }
            refreshInbox(loadOnFailure = true)
        }
    }

    private fun CachedConversationEntity.toDto() = ConversationInboxItem(
        conversationId = conversationId,
        channelId = channelId,
        title = title,
        status = status,
        lastMessageText = lastMessageText,
        lastMessageDirection = lastMessageDirection,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount + unreadDelta,
    )

    private fun ExchangeMessage.toEntity() = CachedChatMessageEntity(
        id = id.ifEmpty { "${createdAt}-${text.hashCode()}" },
        conversationId = conversationId,
        senderId = senderId,
        receiverId = receiverId,
        direction = direction,
        text = text,
        createdAt = createdAt,
        status = status,
    )

    private fun CachedChatMessageEntity.toDto() = ExchangeMessage(
        id = id,
        conversationId = conversationId,
        senderId = senderId.orEmpty(),
        receiverId = receiverId,
        direction = direction,
        text = text,
        createdAt = createdAt,
        status = status,
    )

    companion object {
        private const val TAG = "ChatRepository"
    }
}
