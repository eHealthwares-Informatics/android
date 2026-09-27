package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreatePriceListRequest(
    val code: String,
    val name: String,
    @Json(name = "isDefault") val isDefault: Boolean? = null,
    @Json(name = "isActive") val isActive: Boolean? = null,
    @Json(name = "overrideCodeValidation") val overrideCodeValidation: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class UpdatePriceListRequest(
    val code: String? = null,
    val name: String? = null,
    @Json(name = "isDefault") val isDefault: Boolean? = null,
    @Json(name = "isActive") val isActive: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class CreatePriceListItemRequest(
    @Json(name = "priceListId") val priceListId: String? = null,
    @Json(name = "itemId") val itemId: String,
    @Json(name = "currencyCode") val currencyCode: String? = null,
    @Json(name = "unitPrice") val unitPrice: Double,
)

@JsonClass(generateAdapter = true)
data class UpdatePriceListItemRequest(
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "currencyCode") val currencyCode: String? = null,
    @Json(name = "unitPrice") val unitPrice: Double? = null,
    @Json(name = "startsAt") val startsAt: String? = null,
    @Json(name = "endsAt") val endsAt: String? = null,
)
