package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.AuthResponse
import com.ehealthwares.rxsoft.data.remote.dto.CurrentUserResponse
import com.ehealthwares.rxsoft.data.remote.dto.LoginRequest
import com.ehealthwares.rxsoft.data.remote.dto.RefreshRequest
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

}
