package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerDto(
    val id: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val code: String? = null,
    @Json(name = "addressLine1") val addressLine1: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class CreateCustomerRequest(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    @Json(name = "addressLine1") val addressLine1: String? = null
)
