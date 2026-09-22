package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "accessToken") val accessToken: String,
    @Json(name = "refreshToken") val refreshToken: String,
    @Json(name = "accessTokenExpiresIn") val accessTokenExpiresIn: Int,
    @Json(name = "refreshTokenExpiresIn") val refreshTokenExpiresIn: Int
)

@JsonClass(generateAdapter = true)
data class CurrentUserResponse(
    val id: String,
    val username: String,
    val phone: String? = null,
    val roles: List<String>,
    val permissions: List<String>? = null,
    val modules: List<ModuleInfoDto>? = null
)

@JsonClass(generateAdapter = true)
data class ModuleInfoDto(
    val id: String,
    val code: String? = null,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ShopperRequestOtpRequest(
    val phone: String,
    val channel: String
)

@JsonClass(generateAdapter = true)
data class ShopperVerifyOtpRequest(
    val phone: String
)

@JsonClass(generateAdapter = true)
data class ShopperOtpResponse(
    val sent: Boolean = false,
    val channel: String? = null,
    /** DEV ONLY: the generated code, surfaced so it can be shown on-screen. */
    val code: String? = null
)
