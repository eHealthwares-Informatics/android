package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncMeta(
    val page: Int = 1,
    val limit: Int = 500,
    val total: Int = 0,
    @Json(name = "updatedFrom") val updatedFrom: String? = null,
    val cursor: String? = null,
    @Json(name = "serverTime") val serverTime: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncDeletedRef(
    val id: String,
    @Json(name = "deletedAt") val deletedAt: String? = null,
    @Json(name = "orgItemId") val orgItemId: String? = null,
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "priceListId") val priceListId: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncManifestEntry(
    val changed: Boolean = true,
    @Json(name = "maxUpdatedAt") val maxUpdatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncManifestEntities(
    val items: SyncManifestEntry = SyncManifestEntry(),
    @Json(name = "priceLists") val priceLists: SyncManifestEntry = SyncManifestEntry(),
    @Json(name = "priceListItems") val priceListItems: SyncManifestEntry = SyncManifestEntry(),
    @Json(name = "stockLocations") val stockLocations: SyncManifestEntry = SyncManifestEntry(),
    @Json(name = "stockBalances") val stockBalances: SyncManifestEntry = SyncManifestEntry(),
    val customers: SyncManifestEntry = SyncManifestEntry(),
    val categories: SyncManifestEntry = SyncManifestEntry(),
    val uoms: SyncManifestEntry = SyncManifestEntry(),
)

@JsonClass(generateAdapter = true)
data class SyncManifest(
    @Json(name = "serverTime") val serverTime: String? = null,
    @Json(name = "updatedFrom") val updatedFrom: String? = null,
    val entities: SyncManifestEntities = SyncManifestEntities(),
)

@JsonClass(generateAdapter = true)
data class SyncItemDto(
    val itemId: String,
    @Json(name = "orgItemId") val orgItemId: String? = null,
    val name: String,
    val alias: String? = null,
    val displayName: String? = null,
    val code: String? = null,
    val barcode: String? = null,
    @Json(name = "categoryId") val categoryId: String? = null,
    @Json(name = "categoryCode") val categoryCode: String? = null,
    @Json(name = "categoryName") val categoryName: String? = null,
    @Json(name = "baseUomId") val baseUomId: String? = null,
    @Json(name = "baseUomCode") val baseUomCode: String? = null,
    @Json(name = "baseUomName") val baseUomName: String? = null,
    @Json(name = "saleUomId") val saleUomId: String? = null,
    @Json(name = "saleUomCode") val saleUomCode: String? = null,
    @Json(name = "saleUomName") val saleUomName: String? = null,
    @Json(name = "genericProductCode") val genericProductCode: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "smallImageUrl") val smallImageUrl: String? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncPriceListDto(
    val id: String,
    val code: String,
    val name: String,
    @Json(name = "isDefault") val isDefault: Boolean = false,
    @Json(name = "isActive") val isActive: Boolean = true,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncPriceItemDto(
    val id: String,
    @Json(name = "priceListId") val priceListId: String? = null,
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "currencyCode") val currencyCode: String? = null,
    @Json(name = "unitPrice") val unitPrice: Double = 0.0,
    @Json(name = "startsAt") val startsAt: String? = null,
    @Json(name = "endsAt") val endsAt: String? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncStockLocationDto(
    val id: String,
    val code: String? = null,
    val name: String,
    @Json(name = "locationType") val locationType: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true,
    @Json(name = "parentId") val parentId: String? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncStockBalanceDto(
    val id: String,
    @Json(name = "itemId") val itemId: String? = null,
    @Json(name = "itemCode") val itemCode: String? = null,
    @Json(name = "itemName") val itemName: String? = null,
    @Json(name = "locationId") val locationId: String? = null,
    @Json(name = "locationName") val locationName: String? = null,
    @Json(name = "lotId") val lotId: String? = null,
    @Json(name = "lotCode") val lotCode: String? = null,
    @Json(name = "quantityOnHand") val quantityOnHand: Double = 0.0,
    @Json(name = "quantityReserved") val quantityReserved: Double = 0.0,
    @Json(name = "averageCost") val averageCost: Double? = null,
    @Json(name = "reorderMinQty") val reorderMinQty: Double? = null,
    @Json(name = "reorderMaxQty") val reorderMaxQty: Double? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncCustomerDto(
    val id: String,
    @Json(name = "partyType") val partyType: String? = null,
    val code: String? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    @Json(name = "addressLine1") val addressLine1: String? = null,
    @Json(name = "isActive") val isActive: Boolean = true,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncCategoryDto(
    val id: String,
    val code: String,
    val name: String,
    @Json(name = "parentId") val parentId: String? = null,
    @Json(name = "uomCategoryId") val uomCategoryId: String? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncUomDto(
    val id: String,
    val code: String? = null,
    val name: String,
    @Json(name = "categoryId") val categoryId: String? = null,
    @Json(name = "uomType") val uomType: String? = null,
    val factor: Double = 1.0,
    val rounding: Double = 0.01,
    @Json(name = "isActive") val isActive: Boolean = true,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SyncItemsResponse(
    val data: List<SyncItemDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncPriceListsResponse(
    val data: List<SyncPriceListDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncPriceItemsResponse(
    val data: List<SyncPriceItemDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncStockLocationsResponse(
    val data: List<SyncStockLocationDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncStockBalancesResponse(
    val data: List<SyncStockBalanceDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncCustomersResponse(
    val data: List<SyncCustomerDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncCategoriesResponse(
    val data: List<SyncCategoryDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)

@JsonClass(generateAdapter = true)
data class SyncUomsResponse(
    val data: List<SyncUomDto> = emptyList(),
    val deleted: List<SyncDeletedRef> = emptyList(),
    val meta: SyncMeta = SyncMeta(),
)
