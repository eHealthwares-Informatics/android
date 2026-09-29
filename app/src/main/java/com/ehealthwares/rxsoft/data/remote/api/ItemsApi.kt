package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.*
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

    /** Server caps this endpoint at limit<=100; use it to fetch all pages. */
    @GET("items/dependencies/categories")
    suspend fun getCategories(
        @Query("limit") limit: Int,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
    ): ListResponse<CategoryDto>

    /** Server caps this endpoint at limit<=100; use it to fetch all pages. */
    @GET("items/dependencies/uoms")
    suspend fun getUoms(
        @Query("limit") limit: Int,
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
    ): ListResponse<UomDto>
}
