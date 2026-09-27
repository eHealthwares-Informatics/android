package com.rxsoft.mobile.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

data class ListResponseMeta(
    @Json(name = "page") val page: Int? = null,
    @Json(name = "limit") val limit: Int? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "sortBy") val sortBy: String? = null,
    @Json(name = "sortOrder") val sortOrder: String? = null
)

data class ListResponse<T>(
    val data: List<T>,
    val meta: ListResponseMeta? = null
)

class ListResponseAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = Types.getRawType(type)
        if (rawType != ListResponse::class.java) return null
        val elementType = (type as? ParameterizedType)?.actualTypeArguments?.getOrNull(0) ?: return null
        @Suppress("UNCHECKED_CAST")
        val dataAdapter = moshi.adapter<List<Any>>(
            Types.newParameterizedType(List::class.java, elementType)
        )
        val intAdapter = moshi.adapter<Int>(Int::class.javaObjectType)
        return object : JsonAdapter<ListResponse<Any>>() {
            override fun fromJson(reader: JsonReader): ListResponse<Any> {
                var data: List<Any>? = null
                var page: Int? = null
                var limit: Int? = null
                var total: Int? = null
                var sortBy: String? = null
                var sortOrder: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "data" -> data = dataAdapter.fromJson(reader)
                        "meta" -> {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "page" -> page = intAdapter.fromJson(reader)
                                    "limit" -> limit = intAdapter.fromJson(reader)
                                    "total" -> total = intAdapter.fromJson(reader)
                                    "sortBy" -> sortBy = reader.nextString()
                                    "sortOrder" -> sortOrder = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                return ListResponse(
                    data = data ?: emptyList(),
                    meta = ListResponseMeta(
                        page = page,
                        limit = limit,
                        total = total,
                        sortBy = sortBy,
                        sortOrder = sortOrder
                    )
                )
            }

            override fun toJson(writer: JsonWriter, value: ListResponse<Any>?) {
                if (value == null) {
                    writer.nullValue()
                    return
                }
                writer.beginObject()
                writer.name("data")
                dataAdapter.toJson(writer, value.data)
                writer.name("meta")
                writer.beginObject()
                value.meta?.let { m ->
                    writer.name("page")
                    intAdapter.toJson(writer, m.page)
                    writer.name("limit")
                    intAdapter.toJson(writer, m.limit)
                    writer.name("total")
                    intAdapter.toJson(writer, m.total)
                }
                writer.endObject()
                writer.endObject()
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val success: Boolean,
    val statusCode: Int,
    val path: String?,
    val timestamp: String?,
    val error: ApiErrorDetail?
)

@JsonClass(generateAdapter = true)
data class ApiErrorDetail(
    val message: String?
)

data class PaginationQuery(
    val page: Int = 1,
    val limit: Int = 20,
    val search: String? = null,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc",
    val filter: String? = null
)
