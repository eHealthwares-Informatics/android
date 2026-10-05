package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface PrintApi {
    /**
     * Print to an explicit absolute URL so the configured printer can change at
     * runtime (e.g. after auto-discovery) without rebuilding the DI graph.
     */
    @POST
    suspend fun printReceipt(
        @Url url: String,
        @Body request: ReceiptPrintRequest,
    ): Response<Unit>
}