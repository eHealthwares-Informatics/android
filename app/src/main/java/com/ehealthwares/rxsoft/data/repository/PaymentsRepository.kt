package com.ehealthwares.rxsoft.data.repository

import com.ehealthwares.rxsoft.data.remote.api.PaymentsApi
import com.ehealthwares.rxsoft.data.remote.dto.AvailablePaymentProviderDto
import com.ehealthwares.rxsoft.data.remote.dto.CreatePaymentLinkRequest
import com.ehealthwares.rxsoft.data.remote.dto.CreatePaymentLinkResponse
import com.ehealthwares.rxsoft.data.remote.dto.InitializePaymentRequest
import com.ehealthwares.rxsoft.data.remote.dto.InitializePaymentResponse
import com.ehealthwares.rxsoft.data.remote.dto.SendPaymentLinkRequest
import com.ehealthwares.rxsoft.data.remote.dto.SendPaymentLinkResponse
import com.ehealthwares.rxsoft.data.remote.dto.VerifyPaymentResponse
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

    /** Creates a /shop/pay/:token link for the POS "send payment link" flow. */
    suspend fun createPaymentLink(request: CreatePaymentLinkRequest): Result<CreatePaymentLinkResponse> = try {
        Result.success(paymentsApi.createPaymentLink(request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Sends a payment link over one of the org's configured channels. */
    suspend fun sendPaymentLink(token: String, channelId: String): Result<SendPaymentLinkResponse> = try {
        Result.success(paymentsApi.sendPaymentLink(token, SendPaymentLinkRequest(channelId)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
