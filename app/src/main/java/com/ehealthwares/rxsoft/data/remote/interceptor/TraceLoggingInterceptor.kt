package com.rxsoft.mobile.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.UUID

private const val MAX_BODY_LOG_LENGTH = 2000

private fun redactHeaders(headers: Map<String, String>): Map<String, String> {
    val redacted = headers.toMutableMap()
    for (key in listOf("Authorization", "Cookie", "X-Api-Key", "Token")) {
        if (redacted.containsKey(key)) redacted[key] = "[REDACTED]"
    }
    return redacted
}

private fun bodyToString(body: okhttp3.RequestBody?): String? {
    if (body == null) return null
    val buffer = Buffer()
    body.writeTo(buffer)
    val charset = body.contentType()?.charset() ?: Charsets.UTF_8
    val text = buffer.readString(charset)
    return if (text.length > MAX_BODY_LOG_LENGTH) text.take(MAX_BODY_LOG_LENGTH) + "... (truncated)"
    else text
}

class TraceLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = UUID.randomUUID().toString().take(8)
        val request = chain.request()
        val startNs = System.nanoTime()

        val bodyText = bodyToString(request.body)
        Log.d("HTTP", "--> $requestId ${request.method} ${request.url.encodedPath}" +
                (bodyText?.let { "\n    body: $it" } ?: ""))

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startNs) / 1_000_000
            Log.e("HTTP", "<-- $requestId ${request.method} ${request.url.encodedPath} FAILED ${durationMs}ms - ${e.message}", e)
            throw e
        }

        val durationMs = (System.nanoTime() - startNs) / 1_000_000

        val responseBody = response.body
        val responseBodyText = if (responseBody != null && responseBody.contentLength() != 0L) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            val charset = responseBody.contentType()?.charset() ?: Charsets.UTF_8
            val text = buffer.readString(charset)
            if (text.length > MAX_BODY_LOG_LENGTH) text.take(MAX_BODY_LOG_LENGTH) + "... (truncated)"
            else text
        } else null

        val level = if (response.isSuccessful) Log.DEBUG else Log.WARN
        Log.println(level, "HTTP", "<-- $requestId ${response.code} ${request.method} ${request.url.encodedPath} ${durationMs}ms" +
                (responseBodyText?.let { "\n    body: $it" } ?: ""))

        return response
    }
}
