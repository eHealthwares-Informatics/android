package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.remote.api.InventoryApi
import com.rxsoft.mobile.data.remote.dto.AdjustStockRequest
import com.rxsoft.mobile.data.remote.dto.StockBalanceDto
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val inventoryApi: InventoryApi
) {
    suspend fun getStockBalances(
        search: String? = null,
        page: Int = 1,
        limit: Int = 20,
        categoryCode: String? = null,
    ): Result<List<StockBalanceDto>> {
        return try {
            val params = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
            search?.let { params["search"] = it }
            categoryCode?.let { params["categoryCode"] = it }
            Result.success(inventoryApi.stockBalances(params).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjustStock(request: AdjustStockRequest): Result<StockBalanceDto> {
        return try {
            Result.success(inventoryApi.adjustStock(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Reload the current stock balance for an item at a location. */
    suspend fun findStockBalance(itemId: String, locationId: String): Result<StockBalanceDto?> {
        return try {
            val params = mapOf(
                "itemId" to itemId,
                "locationId" to locationId,
                "limit" to "1",
            )
            Result.success(inventoryApi.stockBalances(params).data.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
