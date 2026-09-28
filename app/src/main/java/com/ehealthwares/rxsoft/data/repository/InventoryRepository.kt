package com.ehealthwares.rxsoft.data.repository

import com.ehealthwares.rxsoft.data.local.PendingStockAdjustmentDao
import com.ehealthwares.rxsoft.data.local.PendingStockAdjustmentEntity
import com.ehealthwares.rxsoft.data.remote.api.InventoryApi
import com.ehealthwares.rxsoft.data.remote.dto.AdjustStockRequest
import com.ehealthwares.rxsoft.data.remote.dto.StockBalanceDto
import com.squareup.moshi.Moshi
import java.util.UUID
import javax.inject.Inject

sealed class StockAdjustResult {
    data class Pushed(val balance: StockBalanceDto) : StockAdjustResult()
    data class Queued(val clientRef: String) : StockAdjustResult()
}

class InventoryRepository @Inject constructor(
    private val inventoryApi: InventoryApi,
    private val pendingStockAdjustmentDao: PendingStockAdjustmentDao,
    private val moshi: Moshi,
) {
    private val adjustRequestAdapter by lazy {
        moshi.adapter(AdjustStockRequest::class.java)
    }
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

    suspend fun adjustStockOrQueue(request: AdjustStockRequest): StockAdjustResult {
        return try {
            StockAdjustResult.Pushed(inventoryApi.adjustStock(request))
        } catch (e: Exception) {
            val clientRef = UUID.randomUUID().toString()
            pendingStockAdjustmentDao.insert(
                PendingStockAdjustmentEntity(
                    clientRef = clientRef,
                    adjustmentJson = adjustRequestAdapter.toJson(request),
                    createdAt = System.currentTimeMillis(),
                ),
            )
            StockAdjustResult.Queued(clientRef)
        }
    }

    suspend fun syncPendingStockAdjustments(): Result<Int> {
        var pushedCount = 0
        for (adj in pendingStockAdjustmentDao.getAll()) {
            try {
                val request = adjustRequestAdapter.fromJson(adj.adjustmentJson) ?: continue
                inventoryApi.adjustStock(request)
                pendingStockAdjustmentDao.deleteByClientRef(adj.clientRef)
                pushedCount++
            } catch (e: Exception) {
                pendingStockAdjustmentDao.markPushFailed(adj.clientRef, e.message ?: "unknown error")
            }
        }
        return Result.success(pushedCount)
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
