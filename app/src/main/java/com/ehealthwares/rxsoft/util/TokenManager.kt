package com.ehealthwares.rxsoft.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tokenStore by preferencesDataStore(name = "auth_tokens")

class TokenManager(private val context: Context) {

    private val userPhoneKey = stringPreferencesKey("user_phone")
    private val accessTokenKey = stringPreferencesKey(Constants.ACCESS_TOKEN_KEY)
    private val refreshTokenKey = stringPreferencesKey(Constants.REFRESH_TOKEN_KEY)

    val accessToken: Flow<String?> = context.tokenStore.data.map { it[accessTokenKey] }
    val refreshToken: Flow<String?> = context.tokenStore.data.map { it[refreshTokenKey] }

    /** Signed-in user's phone — used as the chat sender identity. */
    val userPhone: Flow<String?> = context.tokenStore.data.map { it[userPhoneKey] }


    suspend fun saveTokens(access: String, refresh: String) {
        context.tokenStore.edit { prefs ->
            prefs[accessTokenKey] = access
            prefs[refreshTokenKey] = refresh
        }
    }

    suspend fun saveUserPhone(phone: String?) {
        context.tokenStore.edit { prefs ->
            if (phone.isNullOrBlank()) prefs.remove(userPhoneKey) else prefs[userPhoneKey] = phone
        }
    }

    suspend fun clearTokens() {
        context.tokenStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
            prefs.remove(userPhoneKey)
        }
    }
}
