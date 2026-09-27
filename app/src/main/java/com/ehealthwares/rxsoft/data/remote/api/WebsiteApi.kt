package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.CreateOrderRequest
import com.rxsoft.mobile.data.remote.dto.OrderDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Website orders API (`/api/website/...`).
 */
interface WebsiteApi {
    // Note: the website orders endpoint returns a bare JSON array (not wrapped
    // in { data, meta }), so the return type is a plain List.
    @GET("website/orders")
    suspend fun listOrders(): List<OrderDto>

    @GET("website/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDto

    @POST("website/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): OrderDto
}
