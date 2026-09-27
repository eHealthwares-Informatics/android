package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class StockBalanceDto(
    val id: String,
    val item: ItemDto?,
    val location: StockLocationDto? = null,
    val lot: LotDto? = null,
    @Json(name = "quantityOnHand") val quantityOnHand: BigDecimal,
    @Json(name = "quantityReserved") val quantityReserved: BigDecimal,
    @Json(name = "averageCost") val averageCost: BigDecimal?,
    @Json(name = "reorderMinQty") val reorderMinQty: BigDecimal? = null,
    @Json(name = "reorderMaxQty") val reorderMaxQty: BigDecimal? = null,
)

@JsonClass(generateAdapter = true)
data class LotDto(
    val id: String,
    val code: String,
)

@JsonClass(generateAdapter = true)
data class StockLocationDto(
    val id: String,
    val code: String? = null,
    val name: String,
    @Json(name = "locationType") val locationType: String? = null
)

@JsonClass(generateAdapter = true)
data class AdjustStockRequest(
    @Json(name = "itemId") val itemId: String,
    @Json(name = "locationId") val locationId: String,
    @Json(name = "deltaQuantity") val deltaQuantity: BigDecimal,
    val reason: String
)
