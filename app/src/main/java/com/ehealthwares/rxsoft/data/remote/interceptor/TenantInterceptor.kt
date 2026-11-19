package com.ehealthwares.rxsoft.data.remote.interceptor

import com.ehealthwares.rxsoft.util.JwtClaims
import com.ehealthwares.rxsoft.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Sends the tenant context expected by the backends:
 *
 * - `X-Organization-Id` — the user's organisation (omitted for a super admin,
 *   which means "all organisations" server-side).
 * - `X-Location-Id` — the user's location (omitted when they have none).
 *
 * Normal users are always scoped to their organisation by the backend; these
 * headers make a super admin's selected scope and the location dimension work
 * consistently with the web console.
 */
class TenantInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.accessToken.first() }
        val claims = JwtClaims.decode(token) ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder().apply {
            claims.organizationId?.let { header("X-Organization-Id", it) }
            claims.locationId?.let { header("X-Location-Id", it) }
        }.build()

        return chain.proceed(request)
    }
}
