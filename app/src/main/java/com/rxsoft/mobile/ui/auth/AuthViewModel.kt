package com.rxsoft.mobile.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.repository.AuthRepository
import com.rxsoft.mobile.data.repository.SyncProgress
import com.rxsoft.mobile.data.repository.SyncRepository
import com.rxsoft.mobile.ui.chat.ChatRepository
import com.rxsoft.mobile.util.OfflineSyncManager
import com.rxsoft.mobile.util.PinManager
import com.rxsoft.mobile.util.PosConfigManager
import com.rxsoft.mobile.util.ServerUrlManager
import com.rxsoft.mobile.util.SessionManager
import com.rxsoft.mobile.util.SyncSettingsManager
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

sealed class AppScreen {
    data object Loading : AppScreen()
    data object Login : AppScreen()
    data object ShopperAuth : AppScreen()
    data object PinSetup : AppScreen()
    data object PinUnlock : AppScreen()
    data object Syncing : AppScreen()
    data class Main(val guest: Boolean = false, val shopper: Boolean = false) : AppScreen()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val serverUrlManager: ServerUrlManager,
    val posConfigManager: PosConfigManager,
    private val pinManager: PinManager,
    private val sessionManager: SessionManager,
    private val offlineSyncManager: OfflineSyncManager,
    private val syncRepository: SyncRepository,
    private val syncSettingsManager: SyncSettingsManager,
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _screenState = MutableStateFlow<AppScreen>(AppScreen.Loading)
    val screenState: StateFlow<AppScreen> = _screenState.asStateFlow()

    private val _resetPinTrigger = MutableSharedFlow<Unit>()
    val resetPinTrigger: SharedFlow<Unit> = _resetPinTrigger.asSharedFlow()

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _shopperState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val shopperState: StateFlow<UiState<Unit>> = _shopperState.asStateFlow()

    private val _otpRequested = MutableStateFlow(false)
    val otpRequested: StateFlow<Boolean> = _otpRequested.asStateFlow()

    /** DEV ONLY: last OTP returned by the server, shown on-screen. */
    private val _devOtp = MutableStateFlow<String?>(null)
    val devOtp: StateFlow<String?> = _devOtp.asStateFlow()

    private val _serverUrl = MutableStateFlow(serverUrlManager.getUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    val syncProgress: StateFlow<SyncProgress> = syncRepository.progress

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _canContinueOffline = MutableStateFlow(false)
    val canContinueOffline: StateFlow<Boolean> = _canContinueOffline.asStateFlow()

    private var syncJob: Job? = null

    /** Whether the current session is a self-onboarded mobile shopper. */
    private var shopperSession = false

    init {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            val shopper = authRepository.isMobileShopper()
            shopperSession = shopper
            when {
                !loggedIn -> {
                    Log.d(TAG, "Not logged in, showing guest shop")
                    _screenState.value = AppScreen.Main(guest = true)
                }
                shopper -> {
                    // Shoppers bypass PIN entirely: load the catalog, then enter
                    // the shop shell.
                    Log.d(TAG, "Mobile shopper session — syncing into shop")
                    beginStartupSync()
                }
                !pinManager.hasCredentials() -> {
                    Log.w(TAG, "Logged in but credentials missing, resetting to guest")
                    authRepository.logout()
                    _screenState.value = AppScreen.Main(guest = true)
                }
                !pinManager.isPinSet() -> {
                    Log.d(TAG, "Logged in but no PIN set, showing PIN setup")
                    _screenState.value = AppScreen.PinSetup
                }
                else -> {
                    Log.d(TAG, "Logged in with PIN, showing PIN unlock")
                    _screenState.value = AppScreen.PinUnlock
                }
            }
        }
        viewModelScope.launch {
            sessionManager.checkTimeoutPeriodically()
        }
        offlineSyncManager.start()
        viewModelScope.launch {
            sessionManager.isTimedOut.collect { timedOut ->
                val current = _screenState.value
                if (!timedOut || current !is AppScreen.Main) return@collect
                if (current.guest || current.shopper || shopperSession) {
                    // Shoppers/guests never see the PIN lock after inactivity.
                    if (current.shopper || shopperSession) sessionManager.recordActivity()
                    return@collect
                }
                Log.d(TAG, "Session timed out, showing PIN unlock")
                _screenState.value = AppScreen.PinUnlock
                _resetPinTrigger.emit(Unit)
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
                    shopperSession = false
                    sessionManager.reset()
                    pinManager.saveCredentials(username, password, null)
                    posConfigManager.loadConfig()
                    _loginState.value = UiState.Success(Unit)
                    if (!pinManager.isPinSet()) {
                        _screenState.value = AppScreen.PinSetup
                    } else {
                        beginStartupSync()
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Login failed: ${e.message}")
                    _loginState.value = UiState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun onLoginSuccess() {
        // Consume the success so re-showing the login screen doesn't re-trigger.
        _loginState.value = UiState.Idle
        sessionManager.reset()
        if (!pinManager.isPinSet()) {
            _screenState.value = AppScreen.PinSetup
        } else {
            beginStartupSync()
        }
    }

    // ── Mobile shopper (phone + OTP) ──────────────────────────────────────────

    fun startShopperAuth() {
        _shopperState.value = UiState.Idle
        _otpRequested.value = false
        _devOtp.value = null
        _screenState.value = AppScreen.ShopperAuth
    }

    fun requestShopperOtp(phone: String, channel: String) {
        viewModelScope.launch {
            _shopperState.value = UiState.Loading
            authRepository.requestShopperOtp(phone, channel)
                .onSuccess {
                    _shopperState.value = UiState.Success(Unit)
                    _otpRequested.value = true
                    _devOtp.value = it.code
                }
                .onFailure { e ->
                    Log.e(TAG, "Shopper OTP request failed: ${e.message}")
                    _shopperState.value = UiState.Error(e.message ?: "Could not send code")
                }
        }
    }

    fun verifyShopperOtp(phone: String, code: String) {
        // OTP verification is local: compare against the code the server sent.
        val expected = _devOtp.value
        if (expected != null && code.trim() != expected) {
            _shopperState.value = UiState.Error("Incorrect code")
            return
        }
        viewModelScope.launch {
            _shopperState.value = UiState.Loading
            authRepository.verifyShopperOtp(phone)
                .onSuccess {
                    Log.d(TAG, "Shopper sign-in succeeded")
                    shopperSession = true
                    sessionManager.reset()
                    _shopperState.value = UiState.Success(Unit)
                    _otpRequested.value = false
                    _devOtp.value = null
                    beginStartupSync()
                }
                .onFailure { e ->
                    Log.e(TAG, "Shopper sign-in failed: ${e.message}")
                    _shopperState.value = UiState.Error(e.message ?: "Sign in failed")
                }
        }
    }

    fun onPinAuthenticated() {
        sessionManager.reset()
        shopperSession = false
        beginStartupSync()
    }

    fun onPinCancelled() {
        // "Sign In Again": sign out (clearing the PIN), then go to the sign-in
        // screen with a clean session so the timeout cannot immediately bounce
        // the user back to the PIN screen.
        viewModelScope.launch {
            performLogout()
            _screenState.value = AppScreen.Login
        }
    }

    fun logout() {
        viewModelScope.launch {
            performLogout()
            _screenState.value = AppScreen.Main(guest = true)
        }
    }

    /** Clears the session timeout, tokens, PIN and cached chat state. */
    private suspend fun performLogout() {
        chatRepository.clearLocalCache()
        sessionManager.reset()
        try {
            authRepository.logout()
        } catch (_: Exception) {}
        pinManager.clearAll()
        shopperSession = false
        _loginState.value = UiState.Idle
    }

    /** Open the sign-in screen from guest mode. */
    fun requireLogin() {
        _loginState.value = UiState.Idle
        _shopperState.value = UiState.Idle
        _otpRequested.value = false
        _devOtp.value = null
        _screenState.value = AppScreen.Login
    }

    fun recordActivity() {
        sessionManager.recordActivity()
    }

    // ── Startup sync gate ────────────────────────────────────────────────────

    /** Show the syncing screen, run a (delta) sync bounded by the user timeout. */
    fun beginStartupSync() {
        syncJob?.cancel()
        _syncError.value = null
        _canContinueOffline.value = false
        _screenState.value = AppScreen.Syncing
        syncJob = viewModelScope.launch {
            val timeoutMs = syncSettingsManager.timeoutMillis.first()
            try {
                withTimeout(timeoutMs) { syncRepository.ensureSynced() }
                _screenState.value = AppScreen.Main(shopper = shopperSession)
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Startup sync timed out after ${timeoutMs}ms")
                finishStartupSync("Sync is taking longer than expected.")
            } catch (e: Exception) {
                Log.e(TAG, "Startup sync failed: ${e.message}", e)
                finishStartupSync(e.message ?: "Sync failed")
            }
        }
    }

    /** Continue with whatever is cached, or keep the user on the retry gate. */
    private suspend fun finishStartupSync(message: String) {
        if (syncRepository.hasCache()) {
            Log.w(TAG, "Proceeding with cached data: $message")
            _screenState.value = AppScreen.Main(shopper = shopperSession)
        } else {
            _canContinueOffline.value = true
            _syncError.value = message
        }
    }

    fun retryStartupSync() = beginStartupSync()

    fun continueOffline() {
        _screenState.value = AppScreen.Main(shopper = shopperSession)
    }
}
