package com.ehealthwares.rxsoft.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.local.SyncStateEntity
import com.ehealthwares.rxsoft.data.repository.SyncProgress
import com.ehealthwares.rxsoft.data.repository.SyncRepository
import com.ehealthwares.rxsoft.data.remote.api.PrintApi
import com.ehealthwares.rxsoft.data.remote.dto.AgentInfo
import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintItem
import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintRequest
import com.ehealthwares.rxsoft.util.AppLog
import com.ehealthwares.rxsoft.util.KnownAgent
import com.ehealthwares.rxsoft.util.PosConfigManager
import com.ehealthwares.rxsoft.util.PrinterDiscovery
import com.ehealthwares.rxsoft.util.PrinterUrlManager
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

/** Result of a printer auto-discovery scan, one entry per agent found. */
data class DiscoveredPrinter(
    val ip: String,
    val info: AgentInfo,
) {
    val url: String get() = "http://$ip:${info.port}"
}

/** UI state for the auto-discovery flow. */
sealed interface DiscoveryState {
    data object Idle : DiscoveryState
    data object Scanning : DiscoveryState
    data class Found(val printers: List<DiscoveredPrinter>) : DiscoveryState
    data class Error(val message: String) : DiscoveryState
}

/** Outcome of printing a test page. */
sealed interface TestPrintState {
    data object Idle : TestPrintState
    data object Printing : TestPrintState
    data class Success(val message: String) : TestPrintState
    data class Failure(val message: String) : TestPrintState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serverUrlManager: ServerUrlManager,
    private val moduleConfig: ModuleConfig,
    private val syncSettingsManager: SyncSettingsManager,
    private val syncRepository: SyncRepository,
    val posConfigManager: PosConfigManager,
    private val printerUrlManager: PrinterUrlManager,
    private val printerDiscovery: PrinterDiscovery,
    private val printApi: PrintApi,
) : ViewModel() {

    private val _serverUrl = MutableStateFlow(serverUrlManager.getUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _printerUrl = MutableStateFlow(printerUrlManager.getUrl())
    val printerUrl: StateFlow<String> = _printerUrl.asStateFlow()

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    private val _knownAgents = MutableStateFlow(printerUrlManager.getKnownAgents())
    val knownAgents: StateFlow<List<KnownAgent>> = _knownAgents.asStateFlow()

    private val _testPrintState = MutableStateFlow<TestPrintState>(TestPrintState.Idle)
    val testPrintState: StateFlow<TestPrintState> = _testPrintState.asStateFlow()

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

    fun savePrinterUrl(url: String) {
        printerUrlManager.setUrl(url.trim())
        _printerUrl.value = printerUrlManager.getUrl()
    }

    /** Start a subnet scan for print-agents. */
    fun discoverPrinters() {
        if (_discoveryState.value is DiscoveryState.Scanning) return
        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.Scanning
            val locals = printerDiscovery.localAddresses()
            AppLog.i(TAG, "Scan for print-agents; local addresses=${locals.map { "${it.ip}(${it.interfaceName})" }}")
            if (locals.isEmpty()) {
                AppLog.w(TAG, "No private LAN address found — check Wi-Fi/hotspot is enabled")
                _discoveryState.value = DiscoveryState.Error(
                    "No network detected. Connect to Wi-Fi or enable the hotspot, then retry."
                )
                return@launch
            }
            val found = mutableListOf<DiscoveredPrinter>()
            runCatching {
                printerDiscovery.scan(onFound = { info, ip ->
                    AppLog.i(TAG, "Found print-agent at $ip:${info.port} (${info.printer})")
                    found += DiscoveredPrinter(ip, info)
                })
            }.onSuccess {
                AppLog.i(TAG, "Scan complete: ${found.size} agent(s)")
                _discoveryState.value = if (found.isEmpty()) {
                    DiscoveryState.Error("No printers found on this network.")
                } else {
                    DiscoveryState.Found(found.sortedBy { it.ip.substringAfterLast('.').toIntOrNull() ?: 0 })
                }
            }.onFailure {
                AppLog.e(TAG, "Scan failed", it)
                _discoveryState.value = DiscoveryState.Error(it.message ?: "Discovery failed")
            }
        }
    }

    /** Select a discovered agent: save it as the active printer and remember it. */
    fun selectDiscoveredPrinter(printer: DiscoveredPrinter) {
        val url = printer.url
        AppLog.i(TAG, "Selected printer $url")
        printerUrlManager.setUrl(url)
        printerUrlManager.addKnownAgent(KnownAgent.from(printer.info, printer.ip))
        _printerUrl.value = printerUrlManager.getUrl()
        _knownAgents.value = printerUrlManager.getKnownAgents()
    }

    /** Re-check a single host (used to validate a manually entered URL). */
    fun testPrinterUrl(port: String = PrinterDiscovery.DEFAULT_PORT) {
        if (_discoveryState.value is DiscoveryState.Scanning) return
        viewModelScope.launch {
            _discoveryState.value = DiscoveryState.Scanning
            val host = _printerUrl.value
                .removePrefix("http://").removePrefix("https://")
                .substringBefore('/').substringBefore(':')
            val p = _printerUrl.value.substringAfterLast(':', port).trim().ifBlank { port }
            AppLog.i(TAG, "Probing printer $host:$p")
            val info = printerDiscovery.probe(host, p)
            _discoveryState.value = if (info != null) {
                AppLog.i(TAG, "Printer responded at ${_printerUrl.value}")
                DiscoveryState.Found(listOf(DiscoveredPrinter(host, info)))
            } else {
                AppLog.w(TAG, "No print-agent at ${_printerUrl.value}")
                DiscoveryState.Error("No print-agent responded at ${_printerUrl.value}")
            }
        }
    }

    fun resetDiscovery() {
        _discoveryState.value = DiscoveryState.Idle
    }

    /** Print a diagnostic test page to the currently configured agent. */
    fun printTestPage() {
        if (_testPrintState.value is TestPrintState.Printing) return
        viewModelScope.launch {
            _testPrintState.value = TestPrintState.Printing
            val base = printerUrlManager.getUrl().trimEnd('/')
            val url = "$base/print/receipt"
            val request = ReceiptPrintRequest(
                saleNumber = "TEST-${System.currentTimeMillis() % 100000}",
                header = "PRINT TEST",
                footer = "Test page OK",
                items = listOf(
                    ReceiptPrintItem(name = "Test Item A", qty = 2.0, price = 5.0, total = 10.0),
                    ReceiptPrintItem(name = "Test Item B", qty = 1.0, price = 12.5, total = 12.5),
                ),
                subtotal = 22.5,
                discount = 2.5,
                vat = 4.0,
                total = 24.0,
                paidAmount = 30.0,
                changeAmount = 6.0,
            )
            AppLog.i(TAG, "Test print -> $url")
            runCatching { printApi.printReceipt(url, request) }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        AppLog.i(TAG, "Test print success (${resp.code()})")
                        _testPrintState.value = TestPrintState.Success("Test page sent to $base")
                    } else {
                        AppLog.w(TAG, "Test print failed: HTTP ${resp.code()}")
                        _testPrintState.value = TestPrintState.Failure(
                            "Agent replied HTTP ${resp.code()} ${resp.message().ifBlank { "" }}".trim()
                        )
                    }
                }
                .onFailure { e ->
                    AppLog.e(TAG, "Test print error", e)
                    _testPrintState.value = TestPrintState.Failure(
                        "Could not reach printer agent at $base (${e.message ?: e.javaClass.simpleName})"
                    )
                }
        }
    }

    fun resetTestPrint() {
        _testPrintState.value = TestPrintState.Idle
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

    companion object {
        private const val TAG = "SettingsVM"
    }
}
