package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import org.springframework.http.MediaType
import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.BedCell
import org.starbornag.api.domain.bed.CareAction
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.api.domain.bed.possibleCare
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/** One form of a HAL Schema Forms document (https://github.com/jbadeau/hal-schema-forms). */
data class HalForm(
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>,
    val method: String,
    val contentType: String,
    val schema: ObjectNode
)

/** Builds a bed's `_forms`: one form per care action that is possible right now. */
object HalSchemaForms {
    const val PROFILE = "https://github.com/jbadeau/hal-schema-forms"

    private data class FormSpec(val id: String, val path: String, val command: KClass<out CellCommand>)

    private val specs = mapOf(
        CareAction.PLANT to FormSpec("plant-seedling", "plant", CellCommand.PlantSeedling::class),
        CareAction.WATER to FormSpec("water-cells", "water", CellCommand.Water::class),
        CareAction.FERTILIZE to FormSpec("fertilize-cells", "fertilize", CellCommand.Fertilize::class),
        CareAction.MULCH to FormSpec("mulch-cells", "mulch", CellCommand.Mulch::class),
        CareAction.HARVEST to FormSpec("harvest-crop", "harvest", CellCommand.Harvest::class)
    )

    private const val LOCATION_DESCRIPTION =
        "Cells as spoken: A1, B2 to C4, A1 A3 B5, or 3:2 (letter = row, number = column). Omit for the whole bed."

    private val generator = SchemaGenerator(
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON).with(JacksonModule()).build()
    )

    /**
     * Forms in CareAction order, for the actions [cells] allow. With a [location], the forms act on
     * exactly those cells: the location is fixed in each schema, as its const and default.
     */
    fun forCells(bed: Bed, cells: List<BedCell>, location: String? = null): Map<String, HalForm> {
        val possible = possibleCare(cells)
        return CareAction.entries.filter { it in possible.actions }.associate { action ->
            val spec = specs.getValue(action)
            val schema = schemaFor(spec.command, bed)
            if (action == CareAction.HARVEST) schema.enumerate("plantType", possible.harvestable)
            if (location != null) (schema.with("properties").get("location") as ObjectNode)
                .put("const", location).put("default", location)
            spec.id to HalForm(
                links = mapOf("target" to mapOf("href" to "/api/beds/${bed.id}/${spec.path}")),
                method = "POST",
                contentType = MediaType.APPLICATION_JSON_VALUE,
                schema = schema
            )
        }
    }

    /**
     * The command's JSON Schema, fitted to this bed: the bed id is fixed, the action comes from the
     * target, dates and locations are strings, and "required" follows the Kotlin constructor.
     */
    private fun schemaFor(command: KClass<out CellCommand>, bed: Bed): ObjectNode {
        val schema = generator.generateSchema(command.java)
        schema.remove(listOf("\$defs", "definitions"))
        val properties = schema.with("properties")
        properties.remove("action")
        properties.set<ObjectNode>("bedId", text().put("format", "uuid").put("const", bed.id.toString()))
        properties.set<ObjectNode>("started", text().put("format", "date-time"))
        properties.set<ObjectNode>("location", text().put("description", LOCATION_DESCRIPTION))
        val required = command.primaryConstructor!!.parameters
            .filter { !it.isOptional && !it.type.isMarkedNullable }
            .mapNotNull { it.name }
        schema.putArray("required").apply { required.forEach(::add) }
        return schema
    }

    private fun ObjectNode.enumerate(property: String, values: Set<String>) {
        (with("properties").get(property) as ObjectNode).putArray("enum").apply { values.forEach(::add) }
    }

    private fun text(): ObjectNode = JsonNodeFactory.instance.objectNode().put("type", "string")
}
