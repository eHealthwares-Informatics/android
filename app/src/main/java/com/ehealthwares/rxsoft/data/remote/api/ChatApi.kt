package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.ConversationInboxResponse
import com.rxsoft.mobile.data.remote.dto.ExchangeMessagesResponse
import com.rxsoft.mobile.data.remote.dto.SendWebhookDto
import com.rxsoft.mobile.data.remote.dto.SendWebhookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Conversation Engine (chat) REST endpoints. Registered on the main Retrofit
 * instance — ServerUrlInterceptor retargets the calls at runtime.
 */
interface ChatApi {

    @GET("api/conversations/inbox")
    suspend fun inbox(
        @Query("limit") limit: Int = 30,
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("cursor") cursor: String? = null,
    ): Response<ConversationInboxResponse>

    @GET("api/exchanges")
    suspend fun exchanges(
        @Query("conversationId") conversationId: String,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null,
    ): Response<ExchangeMessagesResponse>

    @POST("api/webhooks/web")
    suspend fun sendWebhook(@Body dto: SendWebhookDto): Response<SendWebhookResponse>

    @POST("api/conversations/{conversationId}/read")
    suspend fun markRead(
        @Path("conversationId") conversationId: String,
        @Query("participantId") participantId: String? = null,
    ): Response<Unit>
}
