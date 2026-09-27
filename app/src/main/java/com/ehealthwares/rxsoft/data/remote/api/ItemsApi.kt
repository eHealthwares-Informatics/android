package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.*
import retrofit2.http.*

interface ItemsApi {
    @GET("items")
    suspend fun listItems(@QueryMap params: Map<String, String>): ListResponse<ItemDto>

    @GET("items/me")
    suspend fun listOrgItems(): List<OrgItemDto>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): ItemDto

    @POST("items")
    suspend fun createItem(@Body request: CreateItemRequest): ItemDto

    @PUT("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body request: PatchItemRequest): ItemDto

    @GET("items/dependencies/categories")
    suspend fun getCategories(): ListResponse<CategoryDto>

    @GET("items/dependencies/uoms")
    suspend fun getUoms(): ListResponse<UomDto>
}
