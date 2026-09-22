package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.local.CachedPriceEntity
import com.rxsoft.mobile.data.local.CachedPriceListEntity
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.local.PriceListDao
import com.rxsoft.mobile.data.remote.api.PricingApi
import com.rxsoft.mobile.data.remote.dto.CreatePriceListRequest
import com.rxsoft.mobile.data.remote.dto.CreatePriceListItemRequest
import com.rxsoft.mobile.data.remote.dto.PriceListDto
import com.rxsoft.mobile.data.remote.dto.PriceListItemDto
import com.rxsoft.mobile.data.remote.dto.UpdatePriceListRequest
import com.rxsoft.mobile.data.remote.dto.UpdatePriceListItemRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Price-list management. Mutations are mirrored into the local SQLite cache so
 * POS/catalog pricing stays fresh without a full sync.
 */
@Singleton
class PricingRepository @Inject constructor(
    private val pricingApi: PricingApi,
    private val priceListDao: PriceListDao,
    private val priceDao: PriceDao,
) {
    suspend fun listPriceLists(): Result<List<PriceListDto>> = try {
        val lists = pricingApi.listPriceLists(mapOf("limit" to "100")).data
        lists.forEach { cacheList(it) }
        Result.success(lists)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listItems(
        priceListId: String,
        search: String? = null,
        page: Int = 1,
        limit: Int = 30,
    ): Result<List<PriceListItemDto>> = try {
        val params = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
        search?.takeIf { it.isNotBlank() }?.let { params["search"] = it }
        Result.success(pricingApi.getPriceListItems(priceListId, params).data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateItemPrice(
        priceListId: String,
        priceListItemId: String,
        itemId: String,
        unitPrice: Double,
        currencyCode: String?,
    ): Result<PriceListItemDto> = try {
        val updated = pricingApi.updatePriceListItem(
            priceListId,
            priceListItemId,
            UpdatePriceListItemRequest(itemId = itemId, unitPrice = unitPrice, currencyCode = currencyCode),
        )
        cacheItem(priceListId, itemId, updated)
        Result.success(updated)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createPriceList(code: String, name: String, isDefault: Boolean): Result<PriceListDto> = try {
        val list = pricingApi.createPriceList(
            CreatePriceListRequest(code = code, name = name, isDefault = isDefault, isActive = true),
        )
        cacheList(list)
        Result.success(list)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePriceList(
        priceListId: String,
        code: String?,
        name: String?,
        isDefault: Boolean?,
        isActive: Boolean?,
    ): Result<PriceListDto> = try {
        val list = pricingApi.updatePriceList(
            priceListId,
            UpdatePriceListRequest(code = code, name = name, isDefault = isDefault, isActive = isActive),
        )
        cacheList(list)
        Result.success(list)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createItem(priceListId: String, itemId: String, unitPrice: Double): Result<PriceListItemDto> = try {
        val created = pricingApi.createPriceListItem(
            CreatePriceListItemRequest(priceListId = priceListId, itemId = itemId, unitPrice = unitPrice),
        )
        cacheItem(priceListId, itemId, created)
        Result.success(created)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun cacheList(list: PriceListDto) {
        val id = list.id ?: return
        priceListDao.upsertAll(
            listOf(
                CachedPriceListEntity(
                    id = id,
                    code = list.code ?: "",
                    name = list.name ?: "",
                    isDefault = list.isDefault == true,
                    isActive = list.isActive != false,
                    updatedAt = null,
                ),
            ),
        )
    }

    private suspend fun cacheItem(priceListId: String, itemId: String, item: PriceListItemDto) {
        val price = item.unitPrice ?: return
        priceDao.upsertAll(
            listOf(
                CachedPriceEntity(
                    priceListId = priceListId,
                    itemId = item.item?.id ?: itemId,
                    unitPrice = price.toPlainString(),
                    currencyCode = item.currencyCode,
                    startsAt = null,
                    endsAt = null,
                    updatedAt = null,
                ),
            ),
        )
    }
}
