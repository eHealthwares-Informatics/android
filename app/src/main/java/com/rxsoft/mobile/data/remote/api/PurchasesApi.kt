package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.CreatePurchaseRequest
import com.rxsoft.mobile.data.remote.dto.ListResponse
import com.rxsoft.mobile.data.remote.dto.PurchaseDto
import com.rxsoft.mobile.data.remote.dto.ReceiveGoodsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

/**
 * Purchases API (`/api/purchases/...`).
 */
interface PurchasesApi {
    @GET("purchases")
    suspend fun listPurchases(@QueryMap params: Map<String, String>): ListResponse<PurchaseDto>

    @GET("purchases/{id}")
    suspend fun getPurchase(@Path("id") id: String): PurchaseDto

    @POST("purchases")
    suspend fun createPurchase(@Body request: CreatePurchaseRequest): PurchaseDto

    @PUT("purchases/{id}")
    suspend fun updatePurchase(@Path("id") id: String, @Body request: UpdatePurchaseStatusRequest): PurchaseDto

    @POST("purchases/{id}/receive")
    suspend fun receiveGoods(@Path("id") id: String, @Body request: ReceiveGoodsRequest): PurchaseDto
}

/** Body for the status-only update used to approve a purchase order. */
data class UpdatePurchaseStatusRequest(
    val status: String,
)
