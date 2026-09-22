package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.AvailablePaymentProviderDto
import com.rxsoft.mobile.data.remote.dto.InitializePaymentRequest
import com.rxsoft.mobile.data.remote.dto.InitializePaymentResponse
import com.rxsoft.mobile.data.remote.dto.VerifyPaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentsApi {
    @GET("payment-providers/available")
    suspend fun availableProviders(): List<AvailablePaymentProviderDto>

    @POST("payments/initialize")
    suspend fun initialize(@Body request: InitializePaymentRequest): InitializePaymentResponse

    @GET("payments/verify/{reference}")
    suspend fun verify(@Path("reference") reference: String): VerifyPaymentResponse
}
