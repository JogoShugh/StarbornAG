package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.RepresentationModel
import org.starbornag.api.domain.bed.command.BedCommand.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

object ObjectMapperSingleton {
    val objectMapper = jacksonObjectMapper()
}

private fun getSchema(action: String) : JsonSchema {
    val commandClass = when (action.lowercase()) { // Determine serializer based on action
        "prepare" -> PrepareBed::class.java
        "plant" -> CellCommand.PlantSeedling::class.java
        "fertilize" -> CellCommand.Fertilize::class.java
        "water" -> CellCommand.Water::class.java
        "harvest" -> CellCommand.Harvest::class.java
        else -> throw Exception("Unknown action type")
    }

    val schema = JsonSchemaGenerator(ObjectMapperSingleton.objectMapper).generateSchema(commandClass)

    return schema
}

fun getDataClassText(kClass: KClass<*>): String {
    return buildString {
        append("data class ${kClass.simpleName}(")
        kClass.memberProperties.forEach { property ->
            val type = property.returnType.jvmErasure.simpleName
            append("val ${property.name}:$type,")
        }
        setLength(length - 1)  // Remove the trailing comma
        append(")")
    }
}

fun generateDataClassesInPackage(vararg classes: KClass<*>): String {
    return classes.filter { it.isData }.joinToString(separator = "") {
        getDataClassText(it)
    }
}


open class BedResource<T>(val id: UUID) : RepresentationModel<BedResource<T>>() {

    init {
        add(
            Links.of(
                Link.of("/api/beds/${id}").withSelfRel(),
                Link.of("/api/beds/${id}/plant", "plant"),
                Link.of("/api/beds/${id}/water").withRel("water"),
                Link.of("/api/beds/${id}/fertilize").withRel("fertilize"),
                Link.of("/api/beds/${id}/harvest").withRel("harvest"),
                Link.of("/api/beds/${id}/history").withRel("history")
            )
        )
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val _linksSchemas = mutableMapOf<String, String>()

    fun includeSchemas() {
        _linksSchemas.putAll(mapOf(
                "plant" to getDataClassText(CellCommand.PlantSeedling::class),
                "water" to getDataClassText(CellCommand.Water::class),
                "fertilize" to getDataClassText(CellCommand.Fertilize::class),
                "harvest" to getDataClassText(CellCommand.Fertilize::class)
            )
        )
    }
}

