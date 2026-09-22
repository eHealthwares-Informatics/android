package com.rxsoft.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.CurrentUserResponse
import com.rxsoft.mobile.data.remote.dto.UserPosConfig
import com.rxsoft.mobile.data.repository.AuthRepository
import com.rxsoft.mobile.util.PosConfigManager
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val posConfigManager: PosConfigManager,
) : ViewModel() {

    private val _user = MutableStateFlow<UiState<CurrentUserResponse>>(UiState.Loading)
    val user: StateFlow<UiState<CurrentUserResponse>> = _user.asStateFlow()

    /** Live POS config: stock location, price list, etc. */
    val posConfig: StateFlow<UserPosConfig?> = posConfigManager.config

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _user.value = UiState.Loading
            authRepository.getCurrentUser()
                .onSuccess { _user.value = UiState.Success(it) }
                .onFailure { _user.value = UiState.Error(it.message ?: "Failed to load profile") }
        }
    }

    /** Reload the profile and refresh the POS configuration (location + price list). */
    fun refresh() {
        posConfigManager.refresh()
        load()
    }
}
