package com.rxsoft.mobile.util

import android.content.Context
import android.content.SharedPreferences
import com.rxsoft.mobile.BuildConfig
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
        return prefs.getString(KEY, null) ?: BuildConfig.API_BASE_URL
    }

    fun setUrl(url: String) {
        prefs.edit().putString(KEY, url).commit()
    }
}
