package com.ehealthwares.rxsoft.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String = "",
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    @Json(name = "sourceEntityType") val sourceEntityType: String? = null,
    @Json(name = "sourceEntityId") val sourceEntityId: String? = null,
    @Json(name = "sourceEntityRef") val sourceEntityRef: String? = null,
    val read: Boolean = false,
    @Json(name = "readAt") val readAt: String? = null,
    @Json(name = "createdAt") val createdAt: String = "",
)

data class NotificationListResponse(
    val data: List<NotificationItem> = emptyList(),
    val meta: ListResponseMeta? = null,
)

@JsonClass(generateAdapter = true)
data class UnreadCountResponse(
    val count: Int = 0,
)
