package com.ehealthwares.rxsoft.data.remote.interceptor

import android.util.Log
import com.ehealthwares.rxsoft.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Never attach a (possibly expired) bearer token to the endpoints that
        // establish a session. A stale token on /auth/login makes the server
        // reject the request even though the credentials are valid, which
        // surfaced as a bogus "session expired" right after entering the PIN.
        val path = original.url.encodedPath
        if (path.contains("auth/login") || path.contains("auth/refresh-token")) {
            Log.d("AuthInterceptor", "Skipping token for auth endpoint: $path")
            return chain.proceed(original)
        }

        val token = runBlocking { tokenManager.accessToken.first() }

        val request = if (token != null) {
            Log.d("AuthInterceptor", "Attaching token to ${original.url.encodedPath}")
           
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d("AuthInterceptor", "No token available for ${original.url.encodedPath}")
            original
        }
        return chain.proceed(request)
    }
}
