package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.command.BedCommand.*
import java.util.*

@RestController
class BedCommandSchemaQueryHandler(
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/api/beds/{bedId}/{action}/schema")
    fun handle(
        @PathVariable bedId: UUID,
        @PathVariable action: String
    ): ResponseEntity<JsonSchema> {
        val commandClass = when (action.lowercase()) { // Determine serializer based on action
            "prepare" -> PrepareBedCommand::class.java
            "plant" -> PlantSeedlingCommand::class.java
            "fertilize" -> FertilizeCommand::class.java
            "water" -> WaterCommand::class.java
            "harvest" -> HarvestCommand::class.java
            else -> return ResponseEntity.notFound().build() // Handle invalid action
        }

        val schema = JsonSchemaGenerator(objectMapper).generateSchema(commandClass)

        return ResponseEntity.ok(schema)
    }
}