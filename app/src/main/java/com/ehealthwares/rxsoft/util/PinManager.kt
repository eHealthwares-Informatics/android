package com.rxsoft.mobile.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class StoredCredentials(
    val username: String,
    val password: String,
    val refreshToken: String?
)

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PinManager"
        private const val PREFS_NAME = "pin_secure_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_FAILED_ATTEMPTS = "pin_failed_attempts"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val PIN_LENGTH = 4
        private const val PBKDF2_ITERATIONS = 10000
        private const val KEY_LENGTH = 256
    }

    /**
     * Encrypted preferences can fail to open (e.g. the underlying Keystore key
     * was invalidated). We degrade gracefully to "no PIN" instead of crashing,
     * which lets the user sign in again and re-create a PIN.
     */
    private val securePrefs: SharedPreferences? by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(
                    KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open encrypted preferences: ${e.message}", e)
            null
        }
    }

    val pinLength: Int get() = PIN_LENGTH

    private fun getString(key: String): String? = try {
        securePrefs?.getString(key, null)
    } catch (e: Exception) {
        Log.e(TAG, "Failed reading '$key': ${e.message}", e)
        null
    }

    /** A usable PIN requires the hash, its salt AND the credentials to re-auth. */
    fun isPinSet(): Boolean =
        getString(KEY_PIN_HASH) != null &&
            getString(KEY_PIN_SALT) != null &&
            hasCredentials()

    fun hasCredentials(): Boolean =
        getString(KEY_USERNAME) != null && getString(KEY_PASSWORD) != null

    fun saveCredentials(username: String, password: String, refreshToken: String?) {
        Log.d(TAG, "Saving credentials for user: $username")
        securePrefs?.edit()
            ?.putString(KEY_USERNAME, username)
            ?.putString(KEY_PASSWORD, password)
            ?.putString(KEY_REFRESH_TOKEN, refreshToken)
            ?.apply()
    }

    fun createPin(pin: String, username: String, password: String, refreshToken: String?) {
        Log.d(TAG, "Creating PIN for user: $username")
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        securePrefs?.edit()
            ?.putString(KEY_PIN_HASH, hash)
            ?.putString(KEY_PIN_SALT, salt)
            ?.putString(KEY_USERNAME, username)
            ?.putString(KEY_PASSWORD, password)
            ?.putString(KEY_REFRESH_TOKEN, refreshToken)
            ?.putInt(KEY_FAILED_ATTEMPTS, 0)
            ?.apply()
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = getString(KEY_PIN_HASH) ?: return false
        val salt = getString(KEY_PIN_SALT) ?: return false
        val hash = hashPin(pin, salt)
        val matches = savedHash == hash
        if (!matches) {
            incrementFailedAttempts()
        } else {
            resetFailedAttempts()
        }
        return matches
    }

    fun getStoredCredentials(): StoredCredentials? {
        val username = getString(KEY_USERNAME) ?: return null
        val password = getString(KEY_PASSWORD) ?: return null
        val refreshToken = getString(KEY_REFRESH_TOKEN)
        return StoredCredentials(username, password, refreshToken)
    }

    fun hasExceededMaxAttempts(): Boolean {
        val attempts = try {
            securePrefs?.getInt(KEY_FAILED_ATTEMPTS, 0) ?: 0
        } catch (e: Exception) {
            0
        }
        return attempts >= MAX_FAILED_ATTEMPTS
    }

    val remainingAttempts: Int
        get() {
            val attempts = try {
                securePrefs?.getInt(KEY_FAILED_ATTEMPTS, 0) ?: 0
            } catch (e: Exception) {
                0
            }
            return MAX_FAILED_ATTEMPTS - attempts
        }

    fun clearAll() {
        Log.d(TAG, "Clearing all PIN data")
        try {
            securePrefs?.edit()?.clear()?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear PIN data: ${e.message}", e)
        }
    }

    private fun hashPin(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt.toByteArray(Charsets.UTF_8), PBKDF2_ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun incrementFailedAttempts() {
        val count = (try { securePrefs?.getInt(KEY_FAILED_ATTEMPTS, 0) } catch (e: Exception) { 0 } ?: 0) + 1
        securePrefs?.edit()?.putInt(KEY_FAILED_ATTEMPTS, count)?.apply()
    }

    private fun resetFailedAttempts() {
        securePrefs?.edit()?.putInt(KEY_FAILED_ATTEMPTS, 0)?.apply()
    }
}
