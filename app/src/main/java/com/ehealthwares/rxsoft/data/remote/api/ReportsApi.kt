package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.DailySalesRow
import com.ehealthwares.rxsoft.data.remote.dto.PurchasesAnalytics
import com.ehealthwares.rxsoft.data.remote.dto.SalesAnalytics
import com.ehealthwares.rxsoft.data.remote.dto.SalesMetrics
import com.ehealthwares.rxsoft.data.remote.dto.TopSellingItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Streaming

interface ReportsApi {
    @GET("reports/daily-sales")
    suspend fun dailySales(@QueryMap params: Map<String, String>): List<DailySalesRow>

    @GET("reports/top-selling-items")
    suspend fun topSellingItems(): List<TopSellingItem>

    @GET("sales/metrics")
    suspend fun salesMetrics(): SalesMetrics

    @GET("reports/sales-analytics")
    suspend fun salesAnalytics(@QueryMap params: Map<String, String>): SalesAnalytics

    @GET("reports/purchases-analytics")
    suspend fun purchasesAnalytics(@QueryMap params: Map<String, String>): PurchasesAnalytics

    @Streaming
    @GET("reports/export")
    suspend fun exportCsv(@QueryMap params: Map<String, String>): Response<ResponseBody>
}
