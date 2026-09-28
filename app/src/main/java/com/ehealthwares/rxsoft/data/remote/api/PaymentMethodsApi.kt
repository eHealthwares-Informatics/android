package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.ListResponse
import com.ehealthwares.rxsoft.data.remote.dto.PaymentMethodDto
import retrofit2.http.GET

interface PaymentMethodsApi {
    @GET("payment-methods")
    suspend fun paymentMethods(): ListResponse<PaymentMethodDto>
}
