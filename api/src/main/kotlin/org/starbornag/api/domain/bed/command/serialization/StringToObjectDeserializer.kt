package org.starbornag.api.domain.bed.command.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

open class StringToObjectDeserializer<T>(
    clazz: Class<T>,
    private val convert: (String) -> T?,
    private val fallbackConvert: (JsonNode) -> T?
) : JsonDeserializer<T>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node: JsonNode = p.readValueAsTree()
        if (node.isTextual) {
            return convert(node.asText())
        } else if (node.isObject) {
            return fallbackConvert(node)
        } else {
            TODO()
        }
    }
}

open class StringToObjectDeserializerWithDefaultFallback<T>(
    private val clazz: Class<T>,
    private val convert: (String) -> T?,
    private val fallback: (String) -> T?
) : JsonDeserializer<T>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node: JsonNode = p.readValueAsTree()
        return if (node.isTextual) {
            convert(node.asText())
        } else if (node.isObject) {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(clazz)
            adapter.fromJson(node.toString())
        } else {
            TODO()
        }
    }
}

open class StringToObjectDeserializerWithPojoFallback<T, F:T>(
    private val convert: (String) -> T?,
    private val clazz: Class<F>
) : JsonDeserializer<T>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T? {
        val node: JsonNode = p.readValueAsTree()
        if (node.isTextual) {
            return convert(node.asText())
        } else if (node.isObject) {
            val mapper = p.codec
            val pojo : F = mapper.treeToValue(node, clazz)
            return pojo
        } else {
            TODO()
        }
    }
}