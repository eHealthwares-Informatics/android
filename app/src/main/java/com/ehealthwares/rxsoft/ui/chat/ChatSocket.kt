package com.rxsoft.mobile.ui.chat

import android.content.Context
import com.rxsoft.mobile.util.ServerUrlManager
import com.rxsoft.mobile.util.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton Socket.IO client for the Conversation Engine gateway
 * (`{conversationHost}/conversations`). Authenticates with the shared JWT via
 * `auth.token`. Surfaces realtime message/inbox events plus
 * `conversation.ended` so screens can reset when a questionnaire completes.
 */
@Singleton
class ChatSocket @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverUrlManager: ServerUrlManager,
    private val tokenManager: TokenManager,
) {
    private var socket: Socket? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    @Synchronized
    fun connect(
        onMessage: (JSONObject) -> Unit,
        onInboxUpdated: (JSONObject) -> Unit,
        onConversationEnded: (JSONObject) -> Unit,
    ) {
        val existing = socket
        if (existing != null) {
            rebind(existing, onMessage, onInboxUpdated, onConversationEnded)
            if (!existing.connected()) existing.connect()
            return
        }

        val token = runBlocking { tokenManager.accessToken.first() }
        val base = serverUrlManager.getConversationUrl().trimEnd('/')
        val options = IO.Options().apply {
            transports = arrayOf("websocket")
            auth = java.util.Collections.singletonMap("token", token.orEmpty())
        }

        socket = try {
            IO.socket(URI.create("$base/conversations"), options)
        } catch (e: Exception) {
            null
        } ?: return

        val s = socket!!
        rebind(s, onMessage, onInboxUpdated, onConversationEnded)
        s.on(Socket.EVENT_CONNECT) {
            _connected.value = true
        }
        s.on(Socket.EVENT_DISCONNECT) {
            _connected.value = false
        }
        s.connect()
    }

    private fun rebind(
        s: Socket,
        onMessage: (JSONObject) -> Unit,
        onInboxUpdated: (JSONObject) -> Unit,
        onConversationEnded: (JSONObject) -> Unit,
    ) {
        s.off("conversation.message.created")
        s.off("conversation.updated")
        s.off("conversation.ended")
        s.on("conversation.message.created") { args ->
            if (args.isNotEmpty()) onMessage(args[0] as? JSONObject ?: JSONObject())
        }
        s.on("conversation.updated") { args ->
            if (args.isNotEmpty()) onInboxUpdated(args[0] as? JSONObject ?: JSONObject())
        }
        s.on("conversation.ended") { args ->
            if (args.isNotEmpty()) onConversationEnded(args[0] as? JSONObject ?: JSONObject())
        }
    }

    @Synchronized
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _connected.value = false
    }

    /** Open a conversation (join its socket room). */
    @Synchronized
    fun openConversation(conversationId: String) {
        socket?.emit("conversation.opened", mapOf("conversationId" to conversationId))
    }

    /** Leave a conversation room. */
    @Synchronized
    fun closeConversation(conversationId: String) {
        socket?.emit("conversation.closed", mapOf("conversationId" to conversationId))
    }

    @Synchronized
    fun isConnected(): Boolean = socket?.connected() == true
}
