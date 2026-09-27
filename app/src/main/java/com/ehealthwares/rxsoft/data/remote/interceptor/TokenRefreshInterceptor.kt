package com.rxsoft.mobile.data.remote.interceptor

import android.util.Log
import com.rxsoft.mobile.data.remote.api.AuthApi
import com.rxsoft.mobile.data.remote.dto.RefreshRequest
import com.rxsoft.mobile.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiProvider: Provider<AuthApi>
) : Interceptor {

    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val path = request.url.encodedPath
            Log.w("TokenRefreshInterceptor", "Got 401 for $path")

            if (path.contains("auth/login") || path.contains("auth/refresh-token")) {
                Log.d("TokenRefreshInterceptor", "Skipping refresh for auth endpoint: $path")
                return response
            }

            synchronized(this) {
                if (isRefreshing) {
                    Log.d("TokenRefreshInterceptor", "Already refreshing, returning original 401 response")
                    return response
                }
                isRefreshing = true
            }

            try {
                Log.d("TokenRefreshInterceptor", "Attempting token refresh")
                val refreshToken = runBlocking { tokenManager.refreshToken.first() }

                if (refreshToken == null) {
                    Log.e("TokenRefreshInterceptor", "No refresh token available")
                    runBlocking { tokenManager.clearTokens() }
                    return response
                }

                Log.d("TokenRefreshInterceptor", "Calling POST /auth/refresh-token")
                val authResponse = runBlocking {
                    authApiProvider.get().refreshToken(RefreshRequest(refreshToken))
                }
                Log.d("TokenRefreshInterceptor", "Refresh OK — new access token: ${authResponse.accessToken.take(20)}...")

                runBlocking {
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                }

                response.close()
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer ${authResponse.accessToken}")
                    .build()
                Log.d("TokenRefreshInterceptor", "Retrying original request with new token")
                return chain.proceed(newRequest)

            } catch (e: Exception) {
                Log.e("TokenRefreshInterceptor", "Token refresh failed: ${e.message}", e)
                runBlocking { tokenManager.clearTokens() }
                return response
            } finally {
                isRefreshing = false
            }
        }

        return response
    }
}
