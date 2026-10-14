package com.ehealthwares.rxsoft.data.remote.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Tolerant adapter for [ReferenceDto]: some endpoints serialize the reference
 * as a full object `{id, code, name}` while others (e.g. the `item.baseUom`
 * field on `GET /inventory/stock-balances`) serialize it as a bare id string.
 * Parse either shape; always emit the object form.
 */
class ReferenceDtoAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): ReferenceDto? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            JsonReader.Token.STRING -> ReferenceDto(id = reader.nextString())
            JsonReader.Token.BEGIN_OBJECT -> {
                var id: String? = null
                var code: String? = null
                var name: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> if (reader.peek() == JsonReader.Token.STRING) id = reader.nextString() else reader.skipValue()
                        "code" -> if (reader.peek() == JsonReader.Token.STRING) code = reader.nextString() else reader.skipValue()
                        "name" -> if (reader.peek() == JsonReader.Token.STRING) name = reader.nextString() else reader.skipValue()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                ReferenceDto(id = id ?: "", code = code, name = name)
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: ReferenceDto?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        writer.name("id").value(value.id)
        if (value.code != null) writer.name("code").value(value.code)
        if (value.name != null) writer.name("name").value(value.name)
        writer.endObject()
    }
}
