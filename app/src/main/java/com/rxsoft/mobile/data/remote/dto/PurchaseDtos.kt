package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

// ── Purchase orders ──────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PurchaseDto(
    val id: String,
    @Json(name = "invoiceNumber") val invoiceNumber: String,
    val supplier: PartyDto?,
    val warehouse: ReferenceDto?,
    @Json(name = "currencyCode") val currencyCode: String = "NGN",
    @Json(name = "totalCost") val totalCost: BigDecimal = BigDecimal.ZERO,
    val status: String = "draft",
    val note: String? = null,
    @Json(name = "orderDate") val orderDate: String? = null,
    val lines: List<PurchaseLineDto>? = null,
)

@JsonClass(generateAdapter = true)
data class PurchaseLineDto(
    val id: String,
    val itemId: String? = null,
    @Json(name = "itemCode") val itemCode: String? = null,
    @Json(name = "itemName") val itemName: String? = null,
    @Json(name = "orderedQty") val orderedQty: BigDecimal = BigDecimal.ZERO,
    @Json(name = "receivedQty") val receivedQty: BigDecimal = BigDecimal.ZERO,
    @Json(name = "uomName") val uomName: String? = null,
    val uomId: String? = null,
    @Json(name = "unitCost") val unitCost: BigDecimal = BigDecimal.ZERO,
    @Json(name = "lineTotal") val lineTotal: BigDecimal = BigDecimal.ZERO,
)

@JsonClass(generateAdapter = true)
data class CreatePurchaseRequest(
    @Json(name = "supplierId") val supplierId: String,
    @Json(name = "warehouseId") val warehouseId: String,
    @Json(name = "currencyCode") val currencyCode: String = "NGN",
    val note: String? = null,
    @Json(name = "invoiceNumber") val invoiceNumber: String? = null,
    val lines: List<CreatePurchaseLine>,
)

@JsonClass(generateAdapter = true)
data class CreatePurchaseLine(
    @Json(name = "itemId") val itemId: String,
    @Json(name = "orderedQty") val orderedQty: BigDecimal,
    @Json(name = "uomId") val uomId: String,
    @Json(name = "unitCost") val unitCost: BigDecimal,
)

@JsonClass(generateAdapter = true)
data class ReceiveGoodsRequest(
    @Json(name = "purchaseOrderId") val purchaseOrderId: String,
    @Json(name = "receivedDate") val receivedDate: String,
    val note: String? = null,
    val lines: List<ReceiveGoodsLine>,
)

@JsonClass(generateAdapter = true)
data class ReceiveGoodsLine(
    @Json(name = "itemId") val itemId: String,
    @Json(name = "receivedQty") val receivedQty: BigDecimal,
    @Json(name = "unitCost") val unitCost: BigDecimal,
    @Json(name = "uomId") val uomId: String,
)
