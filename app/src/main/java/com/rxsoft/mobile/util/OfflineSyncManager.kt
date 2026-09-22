package com.rxsoft.mobile.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.rxsoft.mobile.data.repository.OrdersRepository
import com.rxsoft.mobile.data.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Watches connectivity and, whenever the device comes online:
 *  1. refreshes the offline item cache (org items),
 *  2. pushes any orders queued while offline (oldest first).
 */
@Singleton
class OfflineSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ordersRepository: OrdersRepository,
    private val syncRepository: SyncRepository,
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
        if (_isSyncing.value) return
        _isSyncing.value = true
        try {
            try {
                syncRepository.ensureSynced()
            } catch (e: Exception) {
                Log.w(TAG, "Catalog sync failed: ${e.message}")
            }

            ordersRepository.syncPendingOrders()
                .onSuccess { count ->
                    if (count > 0) Log.d(TAG, "Pushed $count pending order(s)")
                }
                .onFailure { e -> Log.w(TAG, "Order push failed: ${e.message}") }
        } finally {
            _isSyncing.value = false
        }
    }
}
