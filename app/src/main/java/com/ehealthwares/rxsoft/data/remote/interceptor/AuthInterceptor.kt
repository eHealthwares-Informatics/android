package com.rxsoft.mobile.data.remote.interceptor

import android.util.Log
import com.rxsoft.mobile.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
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
