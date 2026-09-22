package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AvailablePaymentProviderDto(
    val id: String,
    val code: String,
    val name: String,
    @Json(name = "providerType") val providerType: String,
    val channel: String? = null,
    val production: Boolean = false,
    val configured: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class InitializePaymentRequest(
    val amount: Double,
    @Json(name = "sourceType") val sourceType: String = "sale",
    @Json(name = "sourceId") val sourceId: String? = null,
    @Json(name = "paymentMethodId") val paymentMethodId: String? = null,
    @Json(name = "providerId") val providerId: String? = null,
    val descriptor: String? = null,
    @Json(name = "customerId") val customerId: String? = null,
    @Json(name = "customerName") val customerName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @Json(name = "returnUrl") val returnUrl: String? = null,
    @Json(name = "callbackUrl") val callbackUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class PaymentProviderRef(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    @Json(name = "providerType") val providerType: String? = null,
)

@JsonClass(generateAdapter = true)
data class InitializePaymentResponse(
    val reference: String,
    @Json(name = "checkoutUrl") val checkoutUrl: String? = null,
    val provider: PaymentProviderRef? = null,
    val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class VerifyPaymentResponse(
    val reference: String,
    val status: String? = null,
    @Json(name = "amountPaid") val amountPaid: Double? = null,
    val provider: String? = null,
    val paid: Boolean = false,
    @Json(name = "paidAt") val paidAt: String? = null,
)
