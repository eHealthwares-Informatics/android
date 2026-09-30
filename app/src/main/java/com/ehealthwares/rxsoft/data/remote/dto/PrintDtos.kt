package com.ehealthwares.rxsoft.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReceiptPrintRequest(
    val saleNumber: String,
    val header: String = "RxSoft",
    val footer: String = "Thank you!",
    val items: List<ReceiptPrintItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val vat: Double = 0.0,
    val total: Double,
    val paidAmount: Double,
    val changeAmount: Double = 0.0,
)

@JsonClass(generateAdapter = true)
data class ReceiptPrintItem(
    val name: String,
    val qty: Double,
    val price: Double,
    val total: Double,
)