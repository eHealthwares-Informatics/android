package com.ehealthwares.rxsoft.data.repository

import android.util.Log
import com.ehealthwares.rxsoft.data.remote.api.AuthApi
import com.ehealthwares.rxsoft.data.remote.dto.LoginRequest
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


    suspend fun logout() {
        Log.d("AuthRepo", "Logging out — clearing tokens")
        tokenManager.clearTokens()
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
