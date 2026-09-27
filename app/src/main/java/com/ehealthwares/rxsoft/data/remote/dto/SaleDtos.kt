package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class SaleDto(
    val id: String,
    @Json(name = "saleNumber") val saleNumber: String,
    @Json(name = "saleChannel") val saleChannel: String,
    val customer: PartyDto?,
    val status: String,
    // @Json(name = "subtotalAmount") val subtotalAmount: BigDecimal,
    // @Json(name = "taxAmount") val taxAmount: BigDecimal,
    @Json(name = "totalAmount") val totalAmount: BigDecimal,
    @Json(name = "paidAmount") val paidAmount: BigDecimal,
    val lines: List<SaleLineDto>?,
    val payments: List<SalePaymentDto>?,
    @Json(name = "saleDate") val saleDate: String,
    @Json(name = "soldBy") val soldBy: UserSummaryDto?,
    val notes: String?
)

@JsonClass(generateAdapter = true)
data class SaleLineDto(
    val id: String?,
    @Json(name = "lineNumber") val lineNumber: Int,
    val item: ItemDto?,
    val quantity: BigDecimal,
    @Json(name = "unitPrice") val unitPrice: BigDecimal,
    @Json(name = "lineTotal") val lineTotal: BigDecimal
)

@JsonClass(generateAdapter = true)
data class SalePaymentDto(
    val id: String?,
    @Json(name = "paymentMethod") val paymentMethod: PaymentMethodDto?,
    val amount: BigDecimal
)

@JsonClass(generateAdapter = true)
data class CreateSaleRequest(
    @Json(name = "saleNumber") val saleNumber: String,
    @Json(name = "saleChannel") val saleChannel: String = "mobile",
    @Json(name = "storeId") val storeId: String,
    @Json(name = "customerId") val customerId: String? = null,
    @Json(name = "stockLocationId") val stockLocationId: String? = null,
    val lines: List<CreateSaleLine>,
    val payments: List<CreateSalePayment>,
    val notes: String? = null,
    @Json(name = "orderDiscountPercent") val orderDiscountPercent: BigDecimal? = null,
    @Json(name = "orderDiscountAmount") val orderDiscountAmount: BigDecimal? = null,
    @Json(name = "taxAmount") val taxAmount: BigDecimal? = null,
    val hold: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class CreateSaleLine(
    @Json(name = "itemId") val itemId: String,
    val quantity: BigDecimal,
    @Json(name = "unitPrice") val unitPrice: BigDecimal,
    @Json(name = "uomId") val uomId: String,
    @Json(name = "uomFactor") val uomFactor: BigDecimal? = null,
    @Json(name = "lotId") val lotId: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateSalePayment(
    @Json(name = "paymentMethodId") val paymentMethodId: String,
    val amount: BigDecimal,
    @Json(name = "paymentReference") val paymentReference: String? = null
)

@JsonClass(generateAdapter = true)
data class PartyDto(
    val id: String,
    val name: String,
    val phone: String? = null,
    val email: String? = null
)

@JsonClass(generateAdapter = true)
data class UserSummaryDto(
    val id: String?,
    val username: String?
)

@JsonClass(generateAdapter = true)
data class PaymentMethodDto(
    val id: String,
    val code: String?,
    val name: String,
    @Json(name = "methodType") val methodType: String,
    @Json(name = "isActive") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class ItemDto(
    val id: String,
    val code: String? = null,
    val name: String,
    val barcode: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "smallImageUrl") val smallImageUrl: String? = null,
    @Json(name = "mediumImageUrl") val mediumImageUrl: String? = null,
    @Json(name = "largeImageUrl") val largeImageUrl: String? = null,
    val category: CategoryDto? = null,
    @Json(name = "baseUomId") val baseUomId: String? = null,
    @Json(name = "saleUomId") val saleUomId: String? = null,
    @Json(name = "purchaseUomId") val purchaseUomId: String? = null,
    @Json(name = "saleUom") val saleUom: ReferenceDto? = null,
    @Json(name = "baseUom") val baseUom: ReferenceDto? = null,
    @Json(name = "purchaseUom") val purchaseUom: ReferenceDto? = null
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val id: String?,
    val name: String?
)

@JsonClass(generateAdapter = true)
data class UomDto(
    val id: String,
    val code: String? = null,
    val name: String,
    @Json(name = "categoryId") val categoryId: String? = null,
    val category: ReferenceDto? = null,
    val factor: BigDecimal = BigDecimal.ONE,
    val rounding: Int? = null,
    @Json(name = "uomType") val uomType: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class ReferenceDto(
    val id: String,
    val code: String? = null,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class SalesMetrics(
    @Json(name = "totalSales") val totalSales: Int,
    @Json(name = "totalRevenue") val totalRevenue: BigDecimal,
    @Json(name = "totalProfit") val totalProfit: BigDecimal?
)
