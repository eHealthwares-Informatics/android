package com.ehealthwares.rxsoft.util

import com.squareup.moshi.JsonReader
import okio.Buffer
import java.util.Base64

/**
 * Tenant information carried by the identity access token.
 *
 * `organizationId` is blank for a super admin (no organisation scope) and is
 * normalised to null here so the interceptor omits the header.
 */
data class TenantClaims(
    val organizationId: String? = null,
    val locationId: String? = null,
    val roles: List<String> = emptyList(),
)

/**
 * Minimal, dependency-light JWT payload reader. The app only needs the tenant
 * claims, so the payload is decoded locally instead of adding organisation and
 * location fields to `/auth/me`.
 */
object JwtClaims {

    fun decode(token: String?): TenantClaims? {
        if (token.isNullOrBlank()) return null
        val parts = token.split('.')
        if (parts.size < 2) return null
        return try {
            val payload = String(
                Base64.getUrlDecoder().decode(pad(parts[1])),
                Charsets.UTF_8,
            )
            parse(payload)
        } catch (_: Exception) {
            null
        }
    }

    private fun pad(value: String): String {
        val remainder = value.length % 4
        return if (remainder == 0) value else value + "=".repeat(4 - remainder)
    }

    private fun parse(payload: String): TenantClaims {
        var organizationId: String? = null
        var locationId: String? = null
        val roles = mutableListOf<String>()

        JsonReader.of(Buffer().writeUtf8(payload)).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "organizationId" -> organizationId = reader.stringOrNull()
                    "locationId" -> locationId = reader.stringOrNull()
                    "roles" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.stringOrNull()?.let(roles::add)
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }

        return TenantClaims(
            organizationId = organizationId?.trim()?.takeIf { it.isNotEmpty() },
            locationId = locationId?.trim()?.takeIf { it.isNotEmpty() },
            roles = roles,
        )
    }

    private fun JsonReader.stringOrNull(): String? {
        if (peek() == JsonReader.Token.NULL) {
            nextNull<Any>()
            return null
        }
        return nextString()
    }
}
