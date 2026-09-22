package com.rxsoft.mobile.data.repository

import android.util.Log
import com.rxsoft.mobile.data.remote.api.AuthApi
import com.rxsoft.mobile.data.remote.dto.LoginRequest
import com.rxsoft.mobile.data.remote.dto.CurrentUserResponse
import com.rxsoft.mobile.data.remote.dto.ShopperOtpResponse
import com.rxsoft.mobile.data.remote.dto.ShopperRequestOtpRequest
import com.rxsoft.mobile.data.remote.dto.ShopperVerifyOtpRequest
import com.rxsoft.mobile.util.TokenManager
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
            tokenManager.setMobileShopper(false)
            tokenManager.saveUserPhone(null)

            Log.d("AuthRepo", "Calling GET /auth/me")
            val user = authApi.me()
            Log.d("AuthRepo", "Me endpoint returned: id=${user.id}, username=${user.username}, roles=${user.roles}")

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login FAILED: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Request an OTP for mobile shopper phone sign-in. */
    suspend fun requestShopperOtp(phone: String, channel: String): Result<ShopperOtpResponse> {
        return try {
            Result.success(authApi.shopperRequestOtp(ShopperRequestOtpRequest(phone, channel)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign in the shopper by phone (the OTP is verified client-side). */
    suspend fun verifyShopperOtp(phone: String): Result<CurrentUserResponse> {
        return try {
            val authResponse = authApi.shopperVerifyOtp(ShopperVerifyOtpRequest(phone))
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            tokenManager.setMobileShopper(true)
            tokenManager.saveUserPhone(phone)
            Result.success(authApi.me())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isMobileShopper(): Boolean = tokenManager.isMobileShopper.first()

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
