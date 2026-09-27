package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.SyncCategoriesResponse
import com.rxsoft.mobile.data.remote.dto.SyncCustomersResponse
import com.rxsoft.mobile.data.remote.dto.SyncItemsResponse
import com.rxsoft.mobile.data.remote.dto.SyncManifest
import com.rxsoft.mobile.data.remote.dto.SyncPriceItemsResponse
import com.rxsoft.mobile.data.remote.dto.SyncPriceListsResponse
import com.rxsoft.mobile.data.remote.dto.SyncStockBalancesResponse
import com.rxsoft.mobile.data.remote.dto.SyncStockLocationsResponse
import com.rxsoft.mobile.data.remote.dto.SyncUomsResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SyncApi {
    @GET("sync/manifest")
    suspend fun manifest(@QueryMap params: Map<String, String>): SyncManifest

    @GET("sync/items")
    suspend fun items(@QueryMap params: Map<String, String>): SyncItemsResponse

    @GET("sync/categories")
    suspend fun categories(@QueryMap params: Map<String, String>): SyncCategoriesResponse

    @GET("sync/uoms")
    suspend fun uoms(@QueryMap params: Map<String, String>): SyncUomsResponse

    @GET("sync/price-lists")
    suspend fun priceLists(@QueryMap params: Map<String, String>): SyncPriceListsResponse

    @GET("sync/price-list-items")
    suspend fun priceListItems(@QueryMap params: Map<String, String>): SyncPriceItemsResponse

    @GET("sync/stock-locations")
    suspend fun stockLocations(@QueryMap params: Map<String, String>): SyncStockLocationsResponse

    @GET("sync/stock-balances")
    suspend fun stockBalances(@QueryMap params: Map<String, String>): SyncStockBalancesResponse

    @GET("sync/customers")
    suspend fun customers(@QueryMap params: Map<String, String>): SyncCustomersResponse
}
