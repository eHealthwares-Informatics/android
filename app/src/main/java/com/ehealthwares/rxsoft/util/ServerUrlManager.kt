package com.ehealthwares.rxsoft.util

import android.content.Context
import android.content.SharedPreferences
import com.ehealthwares.rxsoft.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerUrlManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY = "api_base_url"
        private const val CONVERSATION_KEY = "conversation_base_url"

        /** Production API base URL. */
        const val PRODUCTION_URL = "https://api.ehealthwares.com/api/"

        private const val MODE_KEY = "server_mode"
    }

    /** Build-time default (local emulator) URL. */
    fun defaultUrl(): String = BuildConfig.API_BASE_URL

    /**
     * Ensure a host-only URL carries the API path: `https://host` becomes
     * `https://host/api` (the backend serves under `/api`). URLs that already
     * include a path — e.g. `https://host/api` or a staging prefix — pass
     * through untouched.
     */
    fun normalizeUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        if (trimmed.isEmpty()) return BuildConfig.API_BASE_URL
        if (!trimmed.contains("://")) return trimmed // scheme-less: leave as typed
        val path = try {
            java.net.URI(trimmed).path
        } catch (_: Exception) {
            null
        }
        return if (path.isNullOrBlank() || path == "/") "$trimmed/api" else trimmed
    }

    /** True when [url] points at the production API host (any path). */
    fun isProdUrl(url: String): Boolean {
        val host = hostOf(url)
        val prodHost = hostOf(PRODUCTION_URL)
        return host != null && prodHost != null && host.equals(prodHost, ignoreCase = true)
    }

    /** True when the currently-saved URL points at the production API. */
    fun isProd(): Boolean = isProdUrl(getUrl())

    private fun hostOf(url: String): String? = try {
        java.net.URI(normalizeUrl(url)).host?.lowercase()
    } catch (_: Exception) {
        null
    }

    /** Which server configuration is active. CUSTOM keeps a user-edited URL. */
    enum class Mode { PRODUCTION, CUSTOM }

    /** The active server mode (defaults to PRODUCTION on fresh installs). */
    fun getMode(): Mode = try {
        Mode.valueOf(prefs.getString(MODE_KEY, null) ?: Mode.PRODUCTION.name)
    } catch (_: Exception) {
        Mode.PRODUCTION
    }

    fun setMode(mode: Mode) {
        prefs.edit().putString(MODE_KEY, mode.name).commit()
    }

    /**
     * The user-edited URL kept for CUSTOM mode (never shown in PRODUCTION).
     * Reads fall back to the build default when nothing was saved.
     */
    fun getCustomUrl(): String =
        prefs.getString(KEY, null)?.let { normalizeUrl(it) } ?: BuildConfig.API_BASE_URL

    /** Use the production API. */
    fun useProduction() {
        setMode(Mode.PRODUCTION)
        setUrl(PRODUCTION_URL)
    }

    /** Use a custom (dev/local) URL — [url] defaults to the last saved custom value. */
    fun useCustom(url: String? = null) {
        setMode(Mode.CUSTOM)
        if (url != null) setUrl(url)
    }

    /**
     * Base URL of the Conversation Engine (chat REST + socket). Defaults to
     * the main API host when no explicit conversation URL has been saved.
     */
    fun getConversationUrl(): String {
        return prefs.getString(CONVERSATION_KEY, null)
            ?: getUrl().trimEnd('/').removeSuffix("/api")
    }

    fun setConversationUrl(url: String) {
        prefs.edit().putString(CONVERSATION_KEY, url).commit()
    }

    fun getUrl(): String {
        val raw = prefs.getString(KEY, null) ?: BuildConfig.API_BASE_URL
        // Normalize legacy saved values (host-only URLs get /api appended).
        val normalized = normalizeUrl(raw)
        // One-time migration: legacy installs had a prod URL saved without a
        // mode key — adopt PRODUCTION mode so the switch reflects reality.
        if (prefs.getString(MODE_KEY, null) == null && isProdUrl(normalized)) {
            setMode(Mode.PRODUCTION)
        }
        return normalized
    }

    fun setUrl(url: String) {
        prefs.edit().putString(KEY, normalizeUrl(url)).commit()
    }
}
