package com.rxsoft.mobile.data.remote.interceptor

import com.rxsoft.mobile.util.ServerUrlManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites every request to the currently-saved server URL so that changing the
 * URL in the login screen takes effect immediately (the Retrofit base URL is
 * fixed at app startup and cannot be changed once the client is built).
 *
 * The originally-resolved path (e.g. `/api/auth/login`) and query are preserved
 * while scheme/host/port are taken from the saved URL.
 */
class ServerUrlInterceptor(
    private val serverUrlManager: ServerUrlManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val baseUrl = serverUrlManager.getUrl().trimEnd('/').toHttpUrlOrNull()
            ?: return chain.proceed(original)

        val newUrl = baseUrl.newBuilder()
            .encodedPath(original.url.encodedPath)
            .apply { original.url.encodedQuery?.let { encodedQuery(it) } }
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}