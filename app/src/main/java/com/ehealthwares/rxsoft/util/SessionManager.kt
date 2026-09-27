package com.rxsoft.mobile.util

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    companion object {
        private const val TAG = "SessionManager"
        private const val TIMEOUT_DURATION_MS = 5 * 60 * 1000L
    }

    private val _isTimedOut = MutableStateFlow(false)
    val isTimedOut: StateFlow<Boolean> = _isTimedOut.asStateFlow()

    private var lastActivityTime = System.currentTimeMillis()

    fun recordActivity() {
        lastActivityTime = System.currentTimeMillis()
        if (_isTimedOut.value) {
            Log.d(TAG, "Activity resumed — clearing timeout flag")
            _isTimedOut.value = false
        }
    }

    suspend fun checkTimeoutPeriodically() {
        while (true) {
            delay(30_000L)
            if (!_isTimedOut.value && isPastTimeout()) {
                Log.d(TAG, "Session timed out due to inactivity")
                _isTimedOut.value = true
            }
        }
    }

    private fun isPastTimeout(): Boolean {
        return System.currentTimeMillis() - lastActivityTime > TIMEOUT_DURATION_MS
    }

    fun reset() {
        lastActivityTime = System.currentTimeMillis()
        _isTimedOut.value = false
    }
}
