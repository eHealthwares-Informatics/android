package com.ehealthwares.rxsoft.data.repository

import com.ehealthwares.rxsoft.data.remote.api.CouponsApi
import com.ehealthwares.rxsoft.data.remote.dto.CouponValidationDto
import com.ehealthwares.rxsoft.data.remote.dto.ValidateCouponRequest
import com.ehealthwares.rxsoft.util.toApiException
import javax.inject.Inject
import javax.inject.Singleton

/** Voucher/coupon validation against the backend. */
@Singleton
class CouponsRepository @Inject constructor(
    private val couponsApi: CouponsApi,
) {
    /**
     * Validate a coupon code against a subtotal.
     * Returns the backend verdict (valid flag, reason, discount amount).
     */
    suspend fun validate(code: String, subtotal: Double): Result<CouponValidationDto> = try {
        val response = couponsApi.validate(ValidateCouponRequest(code = code, subtotal = subtotal))
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e.toApiException())
    }
}
