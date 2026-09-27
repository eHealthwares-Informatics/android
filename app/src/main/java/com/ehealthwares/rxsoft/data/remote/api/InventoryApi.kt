package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.AdjustStockRequest
import com.rxsoft.mobile.data.remote.dto.ListResponse
import com.rxsoft.mobile.data.remote.dto.StockBalanceDto
import com.rxsoft.mobile.data.remote.dto.StockLocationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface InventoryApi {
    @GET("inventory/stock-balances")
    suspend fun stockBalances(@QueryMap params: Map<String, String>): ListResponse<StockBalanceDto>

    @GET("stock-locations")
    suspend fun stockLocations(@QueryMap params: Map<String, String>): ListResponse<StockLocationDto>

    @POST("inventory/adjust-quantity")
    suspend fun adjustStock(@Body request: AdjustStockRequest): StockBalanceDto
}
