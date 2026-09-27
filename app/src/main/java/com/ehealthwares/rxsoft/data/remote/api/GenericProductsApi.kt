package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.GenericProductSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Generic-products proxy (`/api/generic-products/...`).
 */
interface GenericProductsApi {
    @GET("generic-products/search")
    suspend fun search(
        @Query("search") search: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): GenericProductSearchResponse
}
