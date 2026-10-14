package com.ehealthwares.rxsoft.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.NotificationItem
import com.ehealthwares.rxsoft.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = repository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val error: StateFlow<String?> = repository.error
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        repository.startPolling()
        viewModelScope.launch { repository.refresh() }
    }

    fun markRead(id: String) {
        viewModelScope.launch { repository.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead() }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }

    fun registerSubscription() {
        viewModelScope.launch { repository.registerSubscription() }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopPolling()
    }
}
