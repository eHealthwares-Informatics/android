package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.AvailablePaymentProviderDto
import com.ehealthwares.rxsoft.data.remote.dto.CreatePaymentLinkRequest
import com.ehealthwares.rxsoft.data.remote.dto.CreatePaymentLinkResponse
import com.ehealthwares.rxsoft.data.remote.dto.InitializePaymentRequest
import com.ehealthwares.rxsoft.data.remote.dto.InitializePaymentResponse
import com.ehealthwares.rxsoft.data.remote.dto.SendPaymentLinkRequest
import com.ehealthwares.rxsoft.data.remote.dto.SendPaymentLinkResponse
import com.ehealthwares.rxsoft.data.remote.dto.VerifyPaymentResponse
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

    /** Creates a /shop/pay/:token payment link (POS "send payment link"). */
    @POST("payment-links")
    suspend fun createPaymentLink(@Body request: CreatePaymentLinkRequest): CreatePaymentLinkResponse

    /** Sends a payment link to the customer over an org-configured channel. */
    @POST("payment-links/{token}/send")
    suspend fun sendPaymentLink(
        @Path("token") token: String,
        @Body request: SendPaymentLinkRequest
    ): SendPaymentLinkResponse
}
