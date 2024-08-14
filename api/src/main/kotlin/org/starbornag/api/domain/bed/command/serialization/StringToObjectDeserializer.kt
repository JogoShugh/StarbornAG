package org.starbornag.api.domain.bed.command.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

open class StringToObjectDeserializer<T>(
    clazz: Class<T>,
    private val convert: (String) -> T?
) : JsonDeserializer<T>() {
    private val adapter : JsonAdapter<T>
    init {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        adapter = moshi.adapter(clazz)
    }
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node: JsonNode = p.readValueAsTree()
        return if (node.isTextual) {
            convert(node.asText())
        } else if (node.isObject) {
            val json = node.toString()
            adapter.fromJson(json)
        } else {
            TODO()
        }
    }
}