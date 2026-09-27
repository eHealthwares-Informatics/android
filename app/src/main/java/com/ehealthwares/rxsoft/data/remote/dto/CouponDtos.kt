package com.ehealthwares.rxsoft.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateCouponRequest(
    val code: String,
    val subtotal: Double,
)

/** Response of `POST /coupons/validate`. */
@JsonClass(generateAdapter = true)
data class CouponValidationDto(
    val valid: Boolean = false,
    val reason: String? = null,
    val code: String? = null,
    val type: String? = null,
    @Json(name = "discountAmount") val discountAmount: Double = 0.0,
    val subtotal: Double? = null,
)
