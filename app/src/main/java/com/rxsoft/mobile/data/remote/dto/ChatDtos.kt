package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** A conversation summary row from `GET /api/conversations/inbox`. */
@JsonClass(generateAdapter = true)
data class ConversationInboxItem(
    val conversationId: String = "",
    val channelId: String = "",
    val title: String? = null,
    val status: String = "ACTIVE",
    val state: String = "ACTIVE",
    @Json(name = "lastMessageText") val lastMessageText: String? = null,
    @Json(name = "lastMessageDirection") val lastMessageDirection: String? = null,
    @Json(name = "lastMessageAt") val lastMessageAt: String? = null,
    @Json(name = "unreadCount") val unreadCount: Int = 0,
)

@JsonClass(generateAdapter = true)
data class ConversationInboxResponse(
    val items: List<ConversationInboxItem> = emptyList(),
    @Json(name = "nextCursor") val nextCursor: String? = null,
)

/** A single message row from `GET /api/exchanges`. */
@JsonClass(generateAdapter = true)
data class ExchangeMessage(
    val id: String = "",
    @Json(name = "conversationId") val conversationId: String = "",
    @Json(name = "senderId") val senderId: String = "",
    @Json(name = "receiverId") val receiverId: String? = null,
    val direction: String = "outbound",
    val text: String = "",
    @Json(name = "questionId") val questionId: String? = null,
    val attribute: String? = null,
    @Json(name = "createdAt") val createdAt: String = "",
    val status: String? = null,
    val optimistic: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class ExchangeMessagesResponse(
    val items: List<ExchangeMessage> = emptyList(),
    @Json(name = "nextCursor") val nextCursor: String? = null,
)

/** Outbound chat message via `POST /api/webhooks/web`. */
@JsonClass(generateAdapter = true)
data class SendWebhookDto(
    @Json(name = "channelId") val channelId: String,
    @Json(name = "senderPhone") val senderPhone: String,
    val text: String,
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "questionnaireCode") val questionnaireCode: String? = null,
)

@JsonClass(generateAdapter = true)
data class SendWebhookResponse(
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "participantId") val participantId: String? = null,
)
