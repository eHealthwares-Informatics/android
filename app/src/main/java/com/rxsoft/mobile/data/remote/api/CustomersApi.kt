package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.CreateCustomerRequest
import com.rxsoft.mobile.data.remote.dto.CustomerDto
import com.rxsoft.mobile.data.remote.dto.ListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CustomersApi {
    @GET("customers")
    suspend fun listCustomers(@QueryMap params: Map<String, String>): ListResponse<CustomerDto>

    @GET("customers/{id}")
    suspend fun getCustomer(@Path("id") id: String): CustomerDto

    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CustomerDto

    @GET("suppliers")
    suspend fun listSuppliers(@QueryMap params: Map<String, String>): ListResponse<CustomerDto>
}
