package com.rxsoft.mobile.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncSettingsDataStore by preferencesDataStore(name = "sync_settings")

/**
 * User-configurable startup sync behaviour. The timeout bounds how long the
 * launch screen waits for the initial/delta catalog sync before continuing
 * with whatever is already cached (or showing a retry when there's no cache).
 */
@Singleton
class SyncSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val DEFAULT_TIMEOUT_SECONDS = 20
        const val MIN_TIMEOUT_SECONDS = 5
        const val MAX_TIMEOUT_SECONDS = 120
        private val KEY_TIMEOUT_SECONDS = intPreferencesKey("startup_sync_timeout_seconds")
    }

    val timeoutSeconds: Flow<Int> = context.syncSettingsDataStore.data.map {
        (it[KEY_TIMEOUT_SECONDS] ?: DEFAULT_TIMEOUT_SECONDS)
            .coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)
    }

    val timeoutMillis: Flow<Long> = timeoutSeconds.map { it * 1000L }

    suspend fun setTimeoutSeconds(seconds: Int) {
        context.syncSettingsDataStore.edit {
            it[KEY_TIMEOUT_SECONDS] = seconds.coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)
        }
    }
}
