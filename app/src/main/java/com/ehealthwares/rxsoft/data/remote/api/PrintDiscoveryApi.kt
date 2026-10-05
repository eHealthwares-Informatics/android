package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.AgentInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Minimal interface to a print-agent's discovery endpoint. A absolute URL is
 * passed per call, since each candidate host has its own origin.
 */
interface PrintDiscoveryApi {
    @GET
    suspend fun discover(@Url url: String): Response<AgentInfo>
}
