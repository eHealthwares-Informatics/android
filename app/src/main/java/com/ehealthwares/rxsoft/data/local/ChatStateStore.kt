package com.rxsoft.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.chatStateStore by preferencesDataStore(name = "chat_state")

/**
 * Small DataStore-backed chat state that survives app restarts: which
 * conversation the user last had open (so ChatScreen can restore the thread
 * without navigating through the inbox) and how many conversations hold
 * unread messages (drives the drawer badge).
 */
class ChatStateStore(private val context: Context) {

    private object Keys {
        val LAST_CONVERSATION_ID = stringPreferencesKey("last_conversation_id")
        val LAST_CONVERSATION_TITLE = stringPreferencesKey("last_conversation_title")
        val LAST_CONVERSATION_ENDED = booleanPreferencesKey("last_conversation_ended")
        val LAST_ACTIVE_AT = longPreferencesKey("last_active_at")
        val CONVERSATIONS_WITH_UNREAD = intPreferencesKey("conversations_with_unread")
    }

    /** Snapshot of the thread the user last viewed. */
    data class LastOpen(
        val conversationId: String,
        val title: String?,
        val ended: Boolean,
        val activeAt: Long,
    )

    val lastOpen: Flow<LastOpen?> = context.chatStateStore.data.map { prefs ->
        val id = prefs[Keys.LAST_CONVERSATION_ID] ?: return@map null
        LastOpen(
            conversationId = id,
            title = prefs[Keys.LAST_CONVERSATION_TITLE],
            ended = prefs[Keys.LAST_CONVERSATION_ENDED] ?: false,
            activeAt = prefs[Keys.LAST_ACTIVE_AT] ?: 0L,
        )
    }

    val conversationsWithUnread: Flow<Int> = context.chatStateStore.data.map { prefs ->
        prefs[Keys.CONVERSATIONS_WITH_UNREAD] ?: 0
    }

    suspend fun saveLastOpen(conversationId: String, title: String?) {
        context.chatStateStore.edit { prefs ->
            prefs[Keys.LAST_CONVERSATION_ID] = conversationId
            if (title.isNullOrBlank()) prefs.remove(Keys.LAST_CONVERSATION_TITLE)
            else prefs[Keys.LAST_CONVERSATION_TITLE] = title
            prefs[Keys.LAST_CONVERSATION_ENDED] = false
            prefs[Keys.LAST_ACTIVE_AT] = System.currentTimeMillis()
        }
    }

    suspend fun saveLastOpenEnded(conversationId: String) {
        context.chatStateStore.edit { prefs ->
            prefs[Keys.LAST_CONVERSATION_ID] = conversationId
            prefs[Keys.LAST_CONVERSATION_ENDED] = true
            prefs[Keys.LAST_ACTIVE_AT] = System.currentTimeMillis()
        }
    }

    suspend fun clearLastOpen() {
        context.chatStateStore.edit { prefs ->
            prefs.remove(Keys.LAST_CONVERSATION_ID)
            prefs.remove(Keys.LAST_CONVERSATION_TITLE)
            prefs.remove(Keys.LAST_CONVERSATION_ENDED)
            prefs.remove(Keys.LAST_ACTIVE_AT)
        }
    }

    suspend fun setConversationsWithUnread(count: Int) {
        context.chatStateStore.edit { prefs ->
            if (count <= 0) prefs.remove(Keys.CONVERSATIONS_WITH_UNREAD)
            else prefs[Keys.CONVERSATIONS_WITH_UNREAD] = count
        }
    }

    suspend fun lastOpenSnapshot(): LastOpen? = lastOpen.first()
}
