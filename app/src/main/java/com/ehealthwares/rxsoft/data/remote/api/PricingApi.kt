package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.CreatePriceListRequest
import com.ehealthwares.rxsoft.data.remote.dto.CreatePriceListItemRequest
import com.ehealthwares.rxsoft.data.remote.dto.ListResponse
import com.ehealthwares.rxsoft.data.remote.dto.PriceListDto
import com.ehealthwares.rxsoft.data.remote.dto.PriceListItemDto
import com.ehealthwares.rxsoft.data.remote.dto.UpdatePriceListRequest
import com.ehealthwares.rxsoft.data.remote.dto.AdjustItemPriceRequest
import com.ehealthwares.rxsoft.data.remote.dto.UpdatePriceListItemRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface PricingApi {
    @GET("price-lists")
    suspend fun listPriceLists(@QueryMap params: Map<String, String>): ListResponse<PriceListDto>

    @POST("price-lists")
    suspend fun createPriceList(@Body request: CreatePriceListRequest): PriceListDto

    @PATCH("price-lists/{priceListId}")
    suspend fun updatePriceList(
        @Path("priceListId") priceListId: String,
        @Body request: UpdatePriceListRequest
    ): PriceListDto

    @GET("price-lists/{priceListId}/items")
    suspend fun getPriceListItems(
        @Path("priceListId") priceListId: String,
        @QueryMap params: Map<String, String>
    ): ListResponse<PriceListItemDto>

    @POST("price-lists/items")
    suspend fun createPriceListItem(@Body request: CreatePriceListItemRequest): PriceListItemDto

    @PATCH("price-lists/{priceListId}/items/{priceListItemId}")
    suspend fun updatePriceListItem(
        @Path("priceListId") priceListId: String,
        @Path("priceListItemId") priceListItemId: String,
        @Body request: UpdatePriceListItemRequest
    ): PriceListItemDto

    @POST("price-lists/adjust-price")
    suspend fun adjustItemPrice(
        @Body request: AdjustItemPriceRequest
    ): PriceListItemDto
}
