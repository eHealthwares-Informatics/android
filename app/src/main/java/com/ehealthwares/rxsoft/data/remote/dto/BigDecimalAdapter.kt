package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalAdapter {
    @FromJson
    fun fromJson(value: Double?): BigDecimal? = value?.let { BigDecimal.valueOf(it) }

    @ToJson
    fun toJson(value: BigDecimal?): Double? = value?.toDouble()
}
