package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

// ── Daily Sales (reports/daily-sales returns an array of per-day rows) ──────

@JsonClass(generateAdapter = true)
data class DailySalesRow(
    val day: String = "",
    @Json(name = "salesCount") val salesCount: Int = 0,
    @Json(name = "totalAmount") val totalAmount: BigDecimal = BigDecimal.ZERO,
)

@JsonClass(generateAdapter = true)
data class PaymentMethodSummary(
    val method: String?,
    val amount: BigDecimal?,
    val count: Int?
)

@JsonClass(generateAdapter = true)
data class TopSellingItem(
    @Json(name = "itemCode") val itemCode: String? = null,
    @Json(name = "quantitySold") val quantitySold: BigDecimal = BigDecimal.ZERO,
    val revenue: BigDecimal = BigDecimal.ZERO,
)

// ── Sales Analytics ──────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class SalesAnalytics(
    @Json(name = "summary") val summary: SalesAnalyticsSummary?,
    @Json(name = "trend") val trend: List<SalesTrendPoint>?,
    @Json(name = "byCategory") val byCategory: List<SalesByCategory>?,
    @Json(name = "byLocation") val byLocation: List<SalesByLocation>?,
)

@JsonClass(generateAdapter = true)
data class SalesAnalyticsSummary(
    @Json(name = "totalRevenue") val totalRevenue: Double = 0.0,
    @Json(name = "totalSales") val totalSales: Int = 0,
    @Json(name = "averageOrderValue") val averageOrderValue: Double = 0.0,
    @Json(name = "itemsSold") val itemsSold: Int = 0,
)

@JsonClass(generateAdapter = true)
data class SalesTrendPoint(
    @Json(name = "day") val day: String,
    @Json(name = "revenue") val revenue: Double = 0.0,
    @Json(name = "orders") val orders: Int = 0,
)

@JsonClass(generateAdapter = true)
data class SalesByCategory(
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "revenue") val revenue: Double = 0.0,
    @Json(name = "orders") val orders: Int = 0,
    @Json(name = "pct") val pct: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class SalesByLocation(
    @Json(name = "stockLocationId") val stockLocationId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "revenue") val revenue: Double = 0.0,
)

// ── Purchases Analytics ──────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PurchasesAnalytics(
    @Json(name = "summary") val summary: PurchasesAnalyticsSummary?,
    @Json(name = "trend") val trend: List<PurchasesTrendPoint>?,
    @Json(name = "byCategory") val byCategory: List<PurchasesByCategory>?,
    @Json(name = "bySupplier") val bySupplier: List<PurchasesBySupplier>?,
    @Json(name = "byLocation") val byLocation: List<PurchasesByLocation>?,
    @Json(name = "byStatus") val byStatus: List<PurchasesByStatus>?,
    @Json(name = "recent") val recent: List<PurchasesRecent>?,
)

@JsonClass(generateAdapter = true)
data class PurchasesAnalyticsSummary(
    @Json(name = "totalValue") val totalValue: Double = 0.0,
    @Json(name = "totalPOs") val totalPOs: Int = 0,
    @Json(name = "itemsPurchased") val itemsPurchased: Int = 0,
    @Json(name = "averagePOValue") val averagePOValue: Double = 0.0,
    @Json(name = "activeSuppliers") val activeSuppliers: Int = 0,
    @Json(name = "topSupplier") val topSupplier: PurchasesTopSupplier? = null,
)

@JsonClass(generateAdapter = true)
data class PurchasesTopSupplier(
    @Json(name = "supplierId") val supplierId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class PurchasesTrendPoint(
    @Json(name = "day") val day: String,
    @Json(name = "value") val value: Double = 0.0,
    @Json(name = "orders") val orders: Int = 0,
)

@JsonClass(generateAdapter = true)
data class PurchasesByCategory(
    @Json(name = "code") val code: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double = 0.0,
    @Json(name = "pct") val pct: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class PurchasesBySupplier(
    @Json(name = "supplierId") val supplierId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class PurchasesByLocation(
    @Json(name = "warehouseId") val warehouseId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "value") val value: Double = 0.0,
    @Json(name = "pct") val pct: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class PurchasesByStatus(
    @Json(name = "status") val status: String? = null,
    @Json(name = "count") val count: Int = 0,
)

@JsonClass(generateAdapter = true)
data class PurchasesRecent(
    @Json(name = "id") val id: String? = null,
    @Json(name = "purchaseOrderNumber") val purchaseOrderNumber: String? = null,
    @Json(name = "orderDate") val orderDate: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "totalAmount") val totalAmount: Double = 0.0,
    @Json(name = "supplierName") val supplierName: String? = null,
)
