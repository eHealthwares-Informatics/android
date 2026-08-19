package com.rxsoft.mobile.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.repository.AuthRepository
import com.rxsoft.mobile.util.PinManager
import com.rxsoft.mobile.util.PosConfigManager
import com.rxsoft.mobile.util.ServerUrlManager
import com.rxsoft.mobile.util.SessionManager
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppScreen {
    data object Loading : AppScreen()
    data object Login : AppScreen()
    data object PinSetup : AppScreen()
    data object PinUnlock : AppScreen()
    data object Main : AppScreen()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val serverUrlManager: ServerUrlManager,
    val posConfigManager: PosConfigManager,
    private val pinManager: PinManager,
    private val sessionManager: SessionManager,
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _screenState = MutableStateFlow<AppScreen>(AppScreen.Loading)
    val screenState: StateFlow<AppScreen> = _screenState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _serverUrl = MutableStateFlow(serverUrlManager.getUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            if (!loggedIn) {
                Log.d(TAG, "Not logged in, showing Login screen")
                _screenState.value = AppScreen.Login
            } else if (!pinManager.isPinSet()) {
                Log.d(TAG, "Logged in but no PIN set, showing PIN setup")
                _screenState.value = AppScreen.PinSetup
            } else {
                Log.d(TAG, "Logged in with PIN, showing PIN unlock")
                _screenState.value = AppScreen.PinUnlock
            }
        }
        viewModelScope.launch {
            sessionManager.checkTimeoutPeriodically()
        }
        viewModelScope.launch {
            sessionManager.isTimedOut.collect { timedOut ->
                if (timedOut && _screenState.value == AppScreen.Main) {
                    Log.d(TAG, "Session timed out, showing PIN unlock")
                    _screenState.value = AppScreen.PinUnlock
                }
            }
        }
    }

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun saveServerUrl(url: String = _serverUrl.value) {
        _serverUrl.value = url
        serverUrlManager.setUrl(url)
    }

    fun login(username: String, password: String) {
        Log.d(TAG, "Login requested for user: $username")
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.login(username, password)
                .onSuccess {
                    Log.d(TAG, "Login succeeded")
                    pinManager.saveCredentials(username, password, null)
                    posConfigManager.loadConfig()
                    _loginState.value = UiState.Success(Unit)
                    _screenState.value =
                        if (!pinManager.isPinSet()) AppScreen.PinSetup else AppScreen.Main
                }
                .onFailure { e ->
                    Log.e(TAG, "Login failed: ${e.message}")
                    _loginState.value = UiState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun onLoginSuccess() {
        _screenState.value =
            if (!pinManager.isPinSet()) AppScreen.PinSetup else AppScreen.Main
    }

    fun onPinAuthenticated() {
        sessionManager.reset()
        _screenState.value = AppScreen.Main
    }

    fun onPinCancelled() {
        pinManager.clearAll()
        logout()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
            pinManager.clearAll()
            _screenState.value = AppScreen.Login
        }
    }

    fun recordActivity() {
        sessionManager.recordActivity()
    }
}
