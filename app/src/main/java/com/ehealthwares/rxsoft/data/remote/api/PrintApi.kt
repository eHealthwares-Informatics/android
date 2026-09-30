package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.ReceiptPrintRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PrintApi {
    @POST("print/receipt")
    suspend fun printReceipt(@Body request: ReceiptPrintRequest): Response<Unit>
}