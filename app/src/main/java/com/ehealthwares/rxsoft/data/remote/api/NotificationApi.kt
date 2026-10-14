package com.ehealthwares.rxsoft.data.remote.api

import com.ehealthwares.rxsoft.data.remote.dto.NotificationListResponse
import com.ehealthwares.rxsoft.data.remote.dto.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("since") since: String? = null,
        @Query("limit") limit: Int = 50,
    ): Response<NotificationListResponse>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): Response<Unit>

    @PATCH("api/notifications/read-all")
    suspend fun markAllRead(): Response<Unit>

    @POST("api/notification-subscriptions")
    suspend fun registerSubscription(@Body body: Map<String, String>? = null): Response<Unit>
}
