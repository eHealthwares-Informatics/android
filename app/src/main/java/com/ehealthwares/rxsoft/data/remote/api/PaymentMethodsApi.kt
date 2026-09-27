package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.ListResponse
import com.rxsoft.mobile.data.remote.dto.PaymentMethodDto
import retrofit2.http.GET

interface PaymentMethodsApi {
    @GET("payment-methods")
    suspend fun paymentMethods(): ListResponse<PaymentMethodDto>
}
