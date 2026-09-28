package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.OrganisationConfig
import com.ehealthwares.rxsoft.data.remote.dto.UserPosConfig
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Body

interface ConfigApi {
    @GET("user-pos-config/me")
    suspend fun userPosConfig(): UserPosConfig

    @GET("organisation-config")
    suspend fun orgConfig(): OrganisationConfig
}
