package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.CouponValidationDto
import com.ehealthwares.rxsoft.data.remote.dto.ValidateCouponRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface CouponsApi {
    /** Public pre-check used by checkout before placing the order. */
    @POST("coupons/validate")
    suspend fun validate(@Body request: ValidateCouponRequest): CouponValidationDto
}
