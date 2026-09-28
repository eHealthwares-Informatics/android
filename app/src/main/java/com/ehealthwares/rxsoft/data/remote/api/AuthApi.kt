package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.AuthResponse
import com.ehealthwares.rxsoft.data.remote.dto.CurrentUserResponse
import com.ehealthwares.rxsoft.data.remote.dto.LoginRequest
import com.ehealthwares.rxsoft.data.remote.dto.RefreshRequest
import com.ehealthwares.rxsoft.data.remote.dto.ShopperOtpResponse
import com.ehealthwares.rxsoft.data.remote.dto.ShopperRequestOtpRequest
import com.ehealthwares.rxsoft.data.remote.dto.ShopperVerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): CurrentUserResponse

    @POST("auth/shopper/request-otp")
    suspend fun shopperRequestOtp(@Body request: ShopperRequestOtpRequest): ShopperOtpResponse

    @POST("auth/shopper/verify-otp")
    suspend fun shopperVerifyOtp(@Body request: ShopperVerifyOtpRequest): AuthResponse
}
