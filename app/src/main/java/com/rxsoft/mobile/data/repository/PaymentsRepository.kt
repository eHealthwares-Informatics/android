package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.remote.api.PaymentsApi
import com.rxsoft.mobile.data.remote.dto.AvailablePaymentProviderDto
import com.rxsoft.mobile.data.remote.dto.InitializePaymentRequest
import com.rxsoft.mobile.data.remote.dto.InitializePaymentResponse
import com.rxsoft.mobile.data.remote.dto.VerifyPaymentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsRepository @Inject constructor(
    private val paymentsApi: PaymentsApi,
) {
    suspend fun availableProviders(): Result<List<AvailablePaymentProviderDto>> = try {
        Result.success(paymentsApi.availableProviders())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun initialize(request: InitializePaymentRequest): Result<InitializePaymentResponse> = try {
        Result.success(paymentsApi.initialize(request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun verify(reference: String): Result<VerifyPaymentResponse> = try {
        Result.success(paymentsApi.verify(reference))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
