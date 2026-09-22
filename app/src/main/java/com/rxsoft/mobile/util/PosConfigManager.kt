package com.rxsoft.mobile.util

import android.util.Log
import com.rxsoft.mobile.data.remote.dto.UserPosConfig
import com.rxsoft.mobile.data.repository.PosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PosConfigManager @Inject constructor(
    private val posRepository: PosRepository
) {
    private val _config = MutableStateFlow<UserPosConfig?>(null)
    val config: StateFlow<UserPosConfig?> = _config.asStateFlow()

    private val _configState = MutableStateFlow<UiState<UserPosConfig>>(UiState.Idle)
    val configState: StateFlow<UiState<UserPosConfig>> = _configState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun loadConfig() {
        if (_configState.value is UiState.Loading) return
        scope.launch {
            _configState.value = UiState.Loading
            Log.d("PosConfigManager", "Loading POS config...")
            posRepository.getUserPosConfig()
                .onSuccess { config ->
                    Log.d("PosConfigManager", "Config loaded: store=${config.storeId}, loc=${config.stockLocation?.name}")
                    _config.value = config
                    _configState.value = UiState.Success(config)
                }
                .onFailure { e ->
                    Log.e("PosConfigManager", "Failed to load config: ${e.message}", e)
                    _configState.value = UiState.Error(e.message ?: "Failed to load POS configuration")
                }
        }
    }

    fun refresh() {
        _configState.value = UiState.Idle
        loadConfig()
    }

    /**
     * Force a fresh fetch of the POS config (server `getOrCreate`), persist it in
     * memory and return it. Used to resolve a missing stock location before
     * starting a sale. Returns null on failure.
     */
    suspend fun fetchConfig(): UserPosConfig? {
        _configState.value = UiState.Loading
        return posRepository.getUserPosConfig()
            .onSuccess { config ->
                Log.d("PosConfigManager", "Config refreshed: store=${config.storeId}, loc=${config.stockLocation?.name}")
                _config.value = config
                _configState.value = UiState.Success(config)
            }
            .getOrElse { e ->
                Log.e("PosConfigManager", "Failed to refresh config: ${e.message}", e)
                _configState.value = UiState.Error(e.message ?: "Failed to load POS configuration")
                null
            }
    }
}
