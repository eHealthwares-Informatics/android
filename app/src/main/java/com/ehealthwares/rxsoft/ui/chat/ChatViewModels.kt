package com.rxsoft.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.ConversationInboxItem
import com.rxsoft.mobile.data.remote.dto.ExchangeMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    val inbox: StateFlow<List<ConversationInboxItem>> = repository.inbox
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val inboxError: StateFlow<String?> = repository.inboxError
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshInbox(loadOnFailure = false)
        }
    }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    val messagesByConversation: StateFlow<Map<String, List<ExchangeMessage>>> =
        repository.messagesByConversation
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Fired when the engine ends the open conversation — the screen resets for a fresh chat. */
    val conversationEnded: SharedFlow<ConversationEnded> = repository.conversationEnded

    /** Restores the persisted ended-state of a conversation from a previous session. */
    suspend fun restoredEndedState(conversationId: String): Boolean =
        repository.isConversationEnded(conversationId)

    /** Every realtime message event — lets the screen follow a newly-started conversation. */
    val messageArrived: SharedFlow<ExchangeMessage> = repository.messageArrived

    fun messagesFor(conversationId: String): List<ExchangeMessage> =
        messagesByConversation.value[conversationId].orEmpty()

    fun loadMessages(conversationId: String, title: String? = null) {
        viewModelScope.launch {
            repository.loadMessages(conversationId)
            repository.markRead(conversationId)
            repository.openConversation(conversationId, title)
        }
    }

    fun onClearedConversation(conversationId: String) {
        viewModelScope.launch {
            repository.closeConversation(conversationId)
        }
    }

    fun sendMessage(conversationId: String?, text: String) {
        viewModelScope.launch {
            repository.sendText(conversationId, text)
        }
    }
}
