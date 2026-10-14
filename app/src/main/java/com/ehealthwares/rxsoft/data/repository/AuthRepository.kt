package com.ehealthwares.rxsoft.data.repository

import android.util.Log
import com.ehealthwares.rxsoft.data.remote.api.AuthApi
import com.ehealthwares.rxsoft.data.remote.dto.FacebookAuthRequest
import com.ehealthwares.rxsoft.data.remote.dto.GoogleAuthRequest
import com.ehealthwares.rxsoft.data.remote.dto.LoginRequest
import com.ehealthwares.rxsoft.data.remote.dto.RefreshRequest
import com.ehealthwares.rxsoft.data.remote.dto.CurrentUserResponse
import com.ehealthwares.rxsoft.util.TokenManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Result<CurrentUserResponse> {
        Log.d("AuthRepo", "Attempting login for user: $username")
        return try {
            Log.d("AuthRepo", "Calling POST /auth/login")
            val authResponse = authApi.login(LoginRequest(username, password))
            Log.d("AuthRepo", "Login OK — access token received: ${authResponse.accessToken.take(20)}...")

            Log.d("AuthRepo", "Saving tokens to DataStore")
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

            Log.d("AuthRepo", "Calling GET /auth/me")
            val user = authApi.me()
            Log.d("AuthRepo", "Me endpoint returned: id=${user.id}, username=${user.username}, roles=${user.roles}")

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }


    /**
     * Google social sign-in: exchange the Credential Manager ID token at the
     * gateway (`auth/oauth/google`, channel='console'). The identity service
     * links any existing account role by the provider-verified email or
     * auto-registers the account; tokens persist through the TokenManager so
     * PIN restore keeps working unchanged.
     */
    suspend fun googleSignIn(idToken: String): Result<CurrentUserResponse> {
        Log.d("AuthRepo", "Google sign-in requested")
        return try {
            val authResponse = authApi.googleSignIn(GoogleAuthRequest(idToken))
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            val user = authApi.me()
            Log.d("AuthRepo", "Google sign-in OK — id=${user.id}, roles=${user.roles}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Google sign-in FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Facebook social sign-in (see [googleSignIn]). */
    suspend fun facebookSignIn(accessToken: String): Result<CurrentUserResponse> {
        Log.d("AuthRepo", "Facebook sign-in requested")
        return try {
            val authResponse = authApi.facebookSignIn(FacebookAuthRequest(accessToken))
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            val user = authApi.me()
            Log.d("AuthRepo", "Facebook sign-in OK — id=${user.id}, roles=${user.roles}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Facebook sign-in FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        Log.d("AuthRepo", "Logging out — clearing tokens")
        tokenManager.clearTokens()
    }

    /**
     * Restore an authenticated session without replaying the password when
     * possible: rotate the stored refresh token first, then fetch the profile.
     * Falls back to a full password login (used when no refresh token is
     * available or it has expired) so PIN unlock keeps working.
     */
    suspend fun restoreSession(username: String, password: String): Result<CurrentUserResponse> {
        val refresh = tokenManager.refreshToken.first()
        if (!refresh.isNullOrBlank()) {
            try {
                Log.d("AuthRepo", "Restoring session via POST /auth/refresh-token")
                val authResponse = authApi.refreshToken(RefreshRequest(refresh))
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

                Log.d("AuthRepo", "Calling GET /auth/me after refresh")
                val user = authApi.me()
                Log.d("AuthRepo", "Session restored via refresh for ${user.username}")
                return Result.success(user)
            } catch (e: Exception) {
                Log.w("AuthRepo", "Refresh-token restore failed, falling back to password login: ${e.message}")
            }
        }
        return login(username, password)
    }

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.accessToken.first()
        Log.d("AuthRepo", "isLoggedIn check: token present = ${token != null}")
        return token != null
    }

    suspend fun getCurrentUser(): Result<CurrentUserResponse> {
        Log.d("AuthRepo", "Fetching current user via GET /auth/me")
        return try {
            val user = authApi.me()
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Get current user failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
