package com.ehealthwares.rxsoft.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.ehealthwares.rxsoft.data.repository.InventoryRepository
import com.ehealthwares.rxsoft.data.repository.OrdersRepository
import com.ehealthwares.rxsoft.data.repository.PosRepository
import com.ehealthwares.rxsoft.data.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Watches connectivity and, whenever the device comes online:
 *  1. refreshes the offline item cache (org items),
 *  2. pushes any orders queued while offline (oldest first).
 *  3. pushes any stock adjustments queued while offline.
 */
@Singleton
class OfflineSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ordersRepository: OrdersRepository,
    private val syncRepository: SyncRepository,
    private val inventoryRepository: InventoryRepository,
    private val posRepository: PosRepository,
) {
    companion object {
        private const val TAG = "OfflineSyncManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var registered = false

    fun start() {
        if (registered) return
        registered = true

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available — refreshing cache and pushing pending orders")
            resetBackoff()
            scope.launch { refreshAndSync() }
        }
    }

    private fun isOnline(): Boolean {
        val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Refresh the offline catalog cache, then push pending orders. */
    suspend fun refreshAndSync() {
        runSyncAttempt()
    }

    /**
     * Runs one full sync attempt (catalog cache + pending pushes).
     * Returns true when every step succeeded, false when any failed.
     */
    private suspend fun runSyncAttempt(): Boolean {
        if (_isSyncing.value) return false
        _isSyncing.value = true
        var ok = true
        try {
            try {
                syncRepository.ensureSynced()
            } catch (e: Exception) {
                ok = false
                Log.w(TAG, "Catalog sync failed: ${e.message}")
            }

            ordersRepository.syncPendingOrders()
                .onSuccess { count ->
                    if (count > 0) Log.d(TAG, "Pushed $count pending order(s)")
                }
                .onFailure { e ->
                    ok = false
                    Log.w(TAG, "Order push failed: ${e.message}")
                }

            inventoryRepository.syncPendingStockAdjustments()
                .onSuccess { count ->
                    if (count > 0) Log.d(TAG, "Pushed $count pending stock adjustment(s)")
                }
                .onFailure { e ->
                    ok = false
                    Log.w(TAG, "Stock adjustment push failed: ${e.message}")
                }

            posRepository.syncPendingSales()
                .onSuccess { count ->
                    if (count > 0) Log.d(TAG, "Pushed $count pending sale(s)")
                }
                .onFailure { e ->
                    ok = false
                    Log.w(TAG, "Pending sale push failed: ${e.message}")
                }
        } finally {
            _isSyncing.value = false
        }
        return ok
    }

    private var backoffJob: Job? = null

    @Volatile
    private var backoffAttempt = 0

    /**
     * Retry sync with exponential backoff (initial, 2x, 4x, ... capped at
     * [maxDelayMs]) until an attempt fully succeeds. A manual retrigger via
     * [AuthViewModel.retryStartupSync] calls [resetBackoff] then this method,
     * which restarts the sequence from the initial delay.
     */
    fun startExponentialBackoff(initialDelayMs: Long = 5_000L, maxDelayMs: Long = 5 * 60_000L) {
        backoffJob?.cancel()
        backoffAttempt = 0
        backoffJob = scope.launch {
            while (true) {
                val shift = backoffAttempt.coerceAtMost(16)
                val delayMs = (initialDelayMs shl shift).coerceAtMost(maxDelayMs)
                Log.d(TAG, "Backoff retry #${backoffAttempt + 1} in ${delayMs}ms")
                delay(delayMs)
                val ok = runSyncAttempt()
                if (ok) {
                    Log.d(TAG, "Backoff sync succeeded — loop stopped")
                    return@launch
                }
                backoffAttempt++
            }
        }
    }

    /** Reset the backoff attempt counter (e.g. when the network returns). */
    fun resetBackoff() {
        backoffAttempt = 0
    }
}
