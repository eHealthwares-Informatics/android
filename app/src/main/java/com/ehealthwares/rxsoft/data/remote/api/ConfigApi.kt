package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.OrganisationConfig
import com.rxsoft.mobile.data.remote.dto.UserPosConfig
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Body

interface ConfigApi {
    @GET("user-pos-config/me")
    suspend fun userPosConfig(): UserPosConfig

    @GET("organisation-config")
    suspend fun orgConfig(): OrganisationConfig
}
