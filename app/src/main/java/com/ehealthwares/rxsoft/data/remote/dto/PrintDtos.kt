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

/** Response from a print-agent's `GET /discover`. */
@JsonClass(generateAdapter = true)
data class AgentInfo(
    val service: String = "",
    val version: String = "",
    val hostname: String = "",
    val ips: List<String> = emptyList(),
    val port: String = "8094",
    val printer: String = "",
    val width: Int = 32,
    val platform: String = "",
)