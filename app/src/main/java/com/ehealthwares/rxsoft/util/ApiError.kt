package com.ehealthwares.rxsoft.util

import com.rxsoft.mobile.data.remote.dto.ApiErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.IOException

/**
 * Maps raw network/HTTP failures onto user-facing exceptions so screens show
 * the real server error (e.g. a 400 stock-gate reason) instead of Retrofit's
 * generic "HTTP 400 ...".
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Extract the server's error message from any Throwable. */
fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is HttpException -> ApiException(extractHttpMessage(this), this)
    is IOException -> ApiException("No connection to server", this)
    else -> ApiException(message ?: "Something went wrong", this)
}

private fun extractHttpMessage(e: HttpException): String {
    val body = try {
        e.response()?.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    val serverMessage = body?.let { parseErrorMessage(it) }
    return serverMessage ?: "Request failed (HTTP ${e.code()})"
}

/**
 * Pull `error.message` (NestJS envelope) from an error body; fall back to the
 * raw body text when it is not the expected JSON shape.
 */
private fun parseErrorMessage(body: String?): String? {
    val raw = body ?: return null
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    return try {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ApiErrorResponse::class.java).lenient()
        adapter.fromJson(trimmed)?.error?.message?.takeIf { it.isNotBlank() }
            ?: trimmed.take(200)
    } catch (_: Exception) {
        trimmed.take(200)
    }
}
