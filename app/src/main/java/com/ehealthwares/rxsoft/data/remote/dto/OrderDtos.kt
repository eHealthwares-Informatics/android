package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

// ── Generic products (existing /api/generic-products/search proxy) ──────────

@JsonClass(generateAdapter = true)
data class GenericProductDto(
    val id: String,
    val code: String,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class GenericProductSearchResponse(
    val data: List<GenericProductDto>,
    val meta: ListResponseMeta? = null,
)

// ── Orders (website API) ─────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: String,
    @Json(name = "orderNumber") val orderNumber: String,
    @Json(name = "orderStatus") val orderStatus: String,
    @Json(name = "totalAmount") val totalAmount: BigDecimal = BigDecimal.ZERO,
    @Json(name = "subtotalAmount") val subtotalAmount: BigDecimal = BigDecimal.ZERO,
    @Json(name = "paymentMethod") val paymentMethod: String? = null,
    val notes: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    val items: List<OrderItemDto>? = null,
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    val id: String,
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "freetextName") val freetextName: String? = null,
    @Json(name = "genericItemCode") val genericItemCode: String? = null,
    val quantity: BigDecimal = BigDecimal.ONE,
    @Json(name = "unitPrice") val unitPrice: BigDecimal = BigDecimal.ZERO,
    val item: OrderItemRefDto? = null,
)

@JsonClass(generateAdapter = true)
data class OrderItemRefDto(
    val id: String,
    val name: String,
    val code: String? = null,
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "paymentMethod") val paymentMethod: String,
    val origin: String = "mobile",
    val items: List<CreateOrderItem>,
)

@JsonClass(generateAdapter = true)
data class CreateOrderItem(
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "freetextName") val freetextName: String? = null,
    @Json(name = "genericItemCode") val genericItemCode: String? = null,
    val quantity: Int,
    @Json(name = "unitPrice") val unitPrice: BigDecimal? = null,
)
