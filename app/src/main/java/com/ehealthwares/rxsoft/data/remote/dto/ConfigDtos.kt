package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class UserPosConfig(
    val id: String?,
    @Json(name = "storeId") val storeId: String?,
    @Json(name = "defaultCustomer") val defaultCustomer: PartyDto?,
    @Json(name = "defaultPriceList") val defaultPriceList: PriceListDto?,
    @Json(name = "stockLocation") val stockLocation: StockLocationDto?,
    @Json(name = "allowPos") val allowPos: Boolean,
    @Json(name = "autoSelectCustomer") val autoSelectCustomer: Boolean,
    @Json(name = "autoSelectPriceList") val autoSelectPriceList: Boolean?,
    @Json(name = "autoSelectLocation") val autoSelectLocation: Boolean?,
    @Json(name = "defaultCustomerId") val defaultCustomerId: String?,
    @Json(name = "defaultPriceListId") val defaultPriceListId: String?,
    @Json(name = "stockLocationId") val stockLocationId: String?,
    @Json(name = "allowA4Print") val allowA4Print: Boolean?,
    @Json(name = "loginTimeoutMinutes") val loginTimeoutMinutes: Int?
)

@JsonClass(generateAdapter = true)
data class OrganisationConfig(
    val id: String?,
    @Json(name = "posHeader") val posHeader: String?,
    @Json(name = "defaultLoginTimeoutMinutes") val defaultLoginTimeoutMinutes: Int
)

@JsonClass(generateAdapter = true)
data class PriceListDto(
    val id: String?,
    val name: String?,
    val code: String?,
    @Json(name = "isDefault") val isDefault: Boolean?,
    @Json(name = "isActive") val isActive: Boolean?
)

@JsonClass(generateAdapter = true)
data class PriceListItemDto(
    val id: String?,
    val priceListId: String?,
    val item: ItemDto?,
    @Json(name = "unitPrice") val unitPrice: BigDecimal?,
    val currencyCode: String?
)
