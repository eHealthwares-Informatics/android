package com.rxsoft.mobile.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.repository.AuthRepository
import com.rxsoft.mobile.util.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PinMode {
    data object SetupCreate : PinMode()
    data object SetupConfirm : PinMode()
    data object Unlock : PinMode()
}

sealed class PinNavigationEvent {
    data object NavigateToHome : PinNavigationEvent()
    data object NavigateToLogin : PinNavigationEvent()
}

@HiltViewModel
class EnterPinViewModel @Inject constructor(
    private val pinManager: PinManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "EnterPinVM"
    }

    private val _mode = MutableStateFlow<PinMode>(PinMode.Unlock)
    val mode: StateFlow<PinMode> = _mode.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _firstPin = MutableStateFlow("")

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Remaining unlock attempts; persists across keystrokes so the countdown stays visible. */
    private val _remainingAttempts = MutableStateFlow<Int?>(null)
    val remainingAttempts: StateFlow<Int?> = _remainingAttempts.asStateFlow()

    private val _shakeTrigger = MutableStateFlow(0)
    val shakeTrigger: StateFlow<Int> = _shakeTrigger.asStateFlow()

    private val _navigation = MutableStateFlow<PinNavigationEvent?>(null)
    val navigation: StateFlow<PinNavigationEvent?> = _navigation.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    init {
        if (!pinManager.isPinSet()) {
            _mode.value = PinMode.SetupCreate
        }
    }

    val isSetupMode: Boolean
        get() = _mode.value is PinMode.SetupCreate || _mode.value is PinMode.SetupConfirm

    val title: String
        get() = when (_mode.value) {
            is PinMode.SetupCreate -> "Create your PIN"
            is PinMode.SetupConfirm -> "Confirm your PIN"
            is PinMode.Unlock -> "Enter your PIN"
        }

    val subtitle: String
        get() = when (_mode.value) {
            is PinMode.SetupCreate -> "Create a PIN for faster and more secure access."
            is PinMode.SetupConfirm -> "Enter the same PIN again to confirm."
            is PinMode.Unlock -> "Enter your PIN to continue."
        }

    fun onDigit(digit: String) {
        if (_pin.value.length >= pinManager.pinLength) return
        if (_isVerifying.value) return
        _error.value = null
        _pin.value += digit

        if (_pin.value.length == pinManager.pinLength) {
            when (_mode.value) {
                is PinMode.SetupCreate -> {
                    _firstPin.value = _pin.value
                    _pin.value = ""
                    _mode.value = PinMode.SetupConfirm
                }
                is PinMode.SetupConfirm -> {
                    if (_pin.value == _firstPin.value) {
                        handlePinMatch()
                    } else {
                        handlePinMismatch("PINs do not match")
                    }
                }
                is PinMode.Unlock -> {
                    handleUnlock()
                }
            }
        }
    }

    fun onDelete() {
        if (_pin.value.isNotEmpty() && !_isVerifying.value) {
            _pin.value = _pin.value.dropLast(1)
            _error.value = null
        }
    }

    fun onCancel() {
        when (_mode.value) {
            is PinMode.SetupConfirm -> {
                _pin.value = ""
                _firstPin.value = ""
                _mode.value = PinMode.SetupCreate
            }
            else -> {
                _navigation.value = PinNavigationEvent.NavigateToLogin
            }
        }
    }

    fun onForgotPin() {
        pinManager.clearAll()
        _navigation.value = PinNavigationEvent.NavigateToLogin
    }

    fun onContinueAfterSetup() {
        if (_pin.value.length == pinManager.pinLength) {
            when (_mode.value) {
                is PinMode.SetupConfirm -> {
                    if (_pin.value == _firstPin.value) {
                        handlePinMatch()
                    } else {
                        handlePinMismatch("PINs do not match")
                    }
                }
                else -> {}
            }
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }

    /**
     * Clears all in-memory PIN entry state (entered digits, first-entry buffer,
     * errors, verification flag and the attempts countdown) and re-syncs the
     * mode with what is actually stored. Used when leaving the PIN screen
     * (e.g. "Sign In Again") so a fresh session starts from a blank PIN pad.
     */
    fun resetState() {
        _pin.value = ""
        _firstPin.value = ""
        _error.value = null
        _shakeTrigger.value = 0
        _isVerifying.value = false
        _remainingAttempts.value = null
        _navigation.value = null
        _mode.value = if (pinManager.isPinSet()) PinMode.Unlock else PinMode.SetupCreate
    }

    private fun handlePinMatch() {
        val creds = pinManager.getStoredCredentials()
        if (creds == null) {
            Log.e(TAG, "No stored credentials found for PIN setup")
            _error.value = "Please sign in again to set up your PIN."
            _pin.value = ""
            _navigation.value = PinNavigationEvent.NavigateToLogin
            return
        }
        pinManager.createPin(_pin.value, creds.username, creds.password, creds.refreshToken)
        _navigation.value = PinNavigationEvent.NavigateToHome
    }

    private fun handlePinMismatch(message: String) {
        _error.value = message
        _shakeTrigger.value++
        _pin.value = ""
    }

    private fun handleUnlock() {
        if (pinManager.hasExceededMaxAttempts()) {
            Log.e(TAG, "PIN attempts exceeded, redirecting to login")
            _error.value = "Too many incorrect attempts. Please sign in again."
            pinManager.clearAll()
            _navigation.value = PinNavigationEvent.NavigateToLogin
            return
        }

        if (!pinManager.verifyPin(_pin.value)) {
            val remaining = pinManager.remainingAttempts
            _remainingAttempts.value = maxOf(remaining, 0)
            _error.value = if (remaining <= 0) {
                "Too many incorrect attempts. Please sign in again."
            } else {
                "Incorrect PIN"
            }
            _shakeTrigger.value++
            _pin.value = ""

            if (remaining <= 0) {
                pinManager.clearAll()
                _navigation.value = PinNavigationEvent.NavigateToLogin
            }
            return
        }

        _isVerifying.value = true
        viewModelScope.launch {
            val creds = pinManager.getStoredCredentials()
            if (creds == null) {
                Log.e(TAG, "No stored credentials after PIN verification")
                _error.value = "Session expired. Please sign in again."
                _isVerifying.value = false
                _pin.value = ""
                _navigation.value = PinNavigationEvent.NavigateToLogin
                return@launch
            }
            val result = authRepository.login(creds.username, creds.password)
            _isVerifying.value = false
            if (result.isSuccess) {
                Log.d(TAG, "Silent re-authentication succeeded")
                _remainingAttempts.value = null
                _navigation.value = PinNavigationEvent.NavigateToHome
            } else {
                // Keep the PIN so the user can retry once the network/server is
                // reachable. Choosing "Sign in again" resets it explicitly.
                Log.e(TAG, "Silent re-authentication failed: ${result.exceptionOrNull()?.message}")
                _error.value = "Couldn't verify your session. Check your connection and try again."
                _pin.value = ""
            }
        }
    }
}
