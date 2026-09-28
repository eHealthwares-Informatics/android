package com.ehealthwares.rxsoft.data.remote.interceptor

import com.ehealthwares.rxsoft.util.ServerUrlManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites every request to the currently-saved server URL so that changing the
 * URL in the login screen takes effect immediately (the Retrofit base URL is
 * fixed at app startup and cannot be changed once the client is built).
 *
 * The endpoint-relative path is recovered by stripping the startup base path
 * (e.g. `/api` from the URL Retrofit was built with) and re-resolved against
 * the saved URL **including its own path** — so `https://host/api` and
 * `https://host/preview/api` both work as expected.
 */
class ServerUrlInterceptor(
    private val serverUrlManager: ServerUrlManager,
    private val startupBasePath: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val savedBase = serverUrlManager.getUrl().trimEnd('/').toHttpUrlOrNull()
            ?: return chain.proceed(original)

        val basePrefix = startupBasePath.trimEnd('/')
        val fullPath = original.url.encodedPath
        val relativePath = if (basePrefix.isNotEmpty() && fullPath.startsWith(basePrefix)) {
            fullPath.substring(basePrefix.length)
        } else {
            fullPath
        }
        val newPath = (savedBase.encodedPath.trimEnd('/') + relativePath)
            .ifEmpty { "/" }

        val newUrl = savedBase.newBuilder()
            .encodedPath(newPath)
            .apply { original.url.encodedQuery?.let { encodedQuery(it) } }
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}