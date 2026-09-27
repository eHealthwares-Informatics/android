package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.CreateSaleRequest
import com.rxsoft.mobile.data.remote.dto.ListResponse
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.data.remote.dto.SalesMetrics
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface SalesApi {
    @GET("sales")
    suspend fun listSales(@QueryMap params: Map<String, String>): ListResponse<SaleDto>

    @GET("sales/{id}")
    suspend fun getSale(@Path("id") id: String): SaleDto

    @POST("sales")
    suspend fun createSale(@Body request: CreateSaleRequest): SaleDto

    @GET("sales/metrics")
    suspend fun salesMetrics(): SalesMetrics
}
