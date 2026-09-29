package com.ehealthwares.rxsoft.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.local.SyncStateEntity
import com.ehealthwares.rxsoft.data.repository.SyncProgress
import com.ehealthwares.rxsoft.data.repository.SyncRepository
import com.ehealthwares.rxsoft.util.PosConfigManager
import com.ehealthwares.rxsoft.util.ServerUrlManager
import com.ehealthwares.rxsoft.util.SyncSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serverUrlManager: ServerUrlManager,
    private val moduleConfig: ModuleConfig,
    private val syncSettingsManager: SyncSettingsManager,
    private val syncRepository: SyncRepository,
    val posConfigManager: PosConfigManager
) : ViewModel() {

    private val _serverUrl = MutableStateFlow(serverUrlManager.getUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _serverMode = MutableStateFlow(serverUrlManager.getMode())
    val serverMode: StateFlow<ServerUrlManager.Mode> = _serverMode.asStateFlow()

    /** The editable URL shown in CUSTOM mode. */
    val customServerUrl: StateFlow<String> = _serverUrl

    fun useProductionServer() {
        serverUrlManager.useProduction()
        _serverMode.value = ServerUrlManager.Mode.PRODUCTION
        _serverUrl.value = serverUrlManager.getUrl()
    }

    fun useCustomServer(url: String? = null) {
        serverUrlManager.useCustom(url)
        _serverMode.value = ServerUrlManager.Mode.CUSTOM
        _serverUrl.value = serverUrlManager.getUrl()
    }

    val activeModules: StateFlow<Set<AppModule>> = moduleConfig.activeModules

    private val _syncTimeoutSeconds =
        MutableStateFlow(SyncSettingsManager.DEFAULT_TIMEOUT_SECONDS)
    val syncTimeoutSeconds: StateFlow<Int> = _syncTimeoutSeconds.asStateFlow()

    val syncProgress: StateFlow<SyncProgress> = syncRepository.progress

    val syncStates: StateFlow<List<SyncStateEntity>> = syncRepository.observeSyncStates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            syncSettingsManager.timeoutSeconds.collect { _syncTimeoutSeconds.value = it }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            runCatching { syncRepository.ensureSynced() }
        }
    }

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun saveServerUrl(url: String) {
        serverUrlManager.useCustom(url.trim())
        _serverMode.value = ServerUrlManager.Mode.CUSTOM
        _serverUrl.value = serverUrlManager.getUrl()
    }



    fun setSyncTimeoutSeconds(seconds: Int) {
        viewModelScope.launch { syncSettingsManager.setTimeoutSeconds(seconds) }
    }

    fun toggleModule(module: AppModule) {
        val current = moduleConfig.activeModules.value.toMutableSet<AppModule>()
        if (current.contains(module)) {
            current.remove(module)
        } else {
            current.add(module)
        }
        moduleConfig.setActiveModules(current)
    }
}
