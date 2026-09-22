package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.remote.api.ReportsApi
import com.rxsoft.mobile.data.remote.dto.DailySalesRow
import com.rxsoft.mobile.data.remote.dto.PurchasesAnalytics
import com.rxsoft.mobile.data.remote.dto.SalesAnalytics
import com.rxsoft.mobile.data.remote.dto.TopSellingItem
import okhttp3.ResponseBody
import javax.inject.Inject

class ReportsRepository @Inject constructor(
    private val reportsApi: ReportsApi
) {
    suspend fun getDailySales(from: String? = null, to: String? = null): Result<List<DailySalesRow>> {
        return try {
            val params = mutableMapOf<String, String>()
            from?.let { params["from"] = it }
            to?.let { params["to"] = it }
            Result.success(reportsApi.dailySales(params))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopSellingItems(): Result<List<TopSellingItem>> {
        return try {
            Result.success(reportsApi.topSellingItems())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalesAnalytics(
        from: String? = null,
        to: String? = null,
        stockLocationId: String? = null,
        categoryCode: String? = null,
    ): Result<SalesAnalytics> {
        return try {
            val params = mutableMapOf<String, String>()
            from?.let { params["from"] = it }
            to?.let { params["to"] = it }
            stockLocationId?.let { params["stockLocationId"] = it }
            categoryCode?.let { params["categoryCode"] = it }
            Result.success(reportsApi.salesAnalytics(params))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPurchasesAnalytics(
        from: String? = null,
        to: String? = null,
        warehouseId: String? = null,
        categoryCode: String? = null,
        supplierId: String? = null,
    ): Result<PurchasesAnalytics> {
        return try {
            val params = mutableMapOf<String, String>()
            from?.let { params["from"] = it }
            to?.let { params["to"] = it }
            warehouseId?.let { params["warehouseId"] = it }
            categoryCode?.let { params["categoryCode"] = it }
            supplierId?.let { params["supplierId"] = it }
            Result.success(reportsApi.purchasesAnalytics(params))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Download the CSV export from the server. */
    suspend fun exportCsv(
        from: String? = null,
        to: String? = null,
    ): Result<ResponseBody> {
        return try {
            val params = mutableMapOf<String, String>()
            from?.let { params["from"] = it }
            to?.let { params["to"] = it }
            val response = reportsApi.exportCsv(params)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Export failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
