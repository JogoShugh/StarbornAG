package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
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
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.rowLetter
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.api.domain.bed.possibleCare
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/** One form of a HAL Schema Forms document (https://github.com/jbadeau/hal-schema-forms). */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class HalForm(
    @get:JsonProperty("_links") val links: Map<String, Map<String, Any>>,
    val method: String,
    /** Required for PATCH, POST and PUT; a GET form has none. */
    val contentType: String?,
    val schema: ObjectNode
) {
    /** Where the form is submitted (a URI template when the target is templated). */
    @get:JsonIgnore
    val target: String get() = links.getValue("target").getValue("href").toString()

    @get:JsonIgnore
    val isCare: Boolean get() = method == "POST"
}

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

    private val generator = SchemaGenerator(
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON).with(JacksonModule()).build()
    )

    /**
     * Forms in CareAction order, for the actions [cells] allow. With a [location], the forms act on
     * exactly those cells: the location is fixed in each schema, as its const and default. Each form
     * posts to [targetBase] followed by the action, such as ".../focus/row/B/water".
     */
    fun forCells(
        bed: Bed,
        cells: List<BedCell>,
        location: String? = null,
        targetBase: String = "/api/beds/${bed.id}"
    ): Map<String, HalForm> {
        val possible = possibleCare(cells)
        return CareAction.entries.filter { it in possible.actions }.associate { action ->
            val spec = specs.getValue(action)
            val schema = FormWords.describe(spec.id, schemaFor(spec.command, bed))
            if (action == CareAction.HARVEST) {
                schema.enumerate("plantType", possible.harvestable)
                schema.enumerate("plantCultivar", possible.harvestableCultivars)
            }
            if (location != null) (schema.with("properties").get("location") as ObjectNode)
                .put("const", location).put("default", location)
            spec.id to HalForm(
                links = mapOf("target" to mapOf("href" to "$targetBase/${spec.path}")),
                method = "POST",
                contentType = MediaType.APPLICATION_JSON_VALUE,
                schema = schema
            )
        }
    }

    /**
     * GET forms that go straight to a place inside [focus] (HAL Schema Forms: a templated target whose
     * fields fill the template). The schemas bound each field to the places that exist, so an agent
     * never builds an address itself: rows by letter (enum), columns by number (minimum to maximum).
     * The whole bed offers rows, columns and cells; a row or a column only its own cells.
     */
    fun goTo(bedId: UUID, focus: Focus, rows: Int, columns: Int): Map<String, HalForm> {
        val base = "/beds/$bedId/focus"
        val letters = (1..rows).map { rowLetter(it) }
        val cellRows = if (focus is Focus.OnRow) listOf(rowLetter(focus.row)) else letters
        val cellColumns = if (focus is Focus.OnColumn) focus.column..focus.column else 1..columns
        val cell = "go-to-cell" to goForm(
            "$base/cell/{row}{column}", "Go to a cell",
            "row" to letterField(cellRows), "column" to numberField(cellColumns)
        )
        return when (focus) {
            Focus.OnBed -> mapOf(
                "go-to-row" to goForm("$base/row/{row}", "Go to a row", "row" to letterField(letters)),
                "go-to-column" to goForm(
                    "$base/column/{column}", "Go to a column", "column" to numberField(1..columns)
                ),
                cell
            )
            is Focus.OnRow, is Focus.OnColumn -> mapOf(cell)
            is Focus.OnCell -> emptyMap()
        }
    }

    private fun goForm(target: String, title: String, vararg fields: Pair<String, ObjectNode>): HalForm {
        val schema = JsonNodeFactory.instance.objectNode()
            .put("\$schema", "https://json-schema.org/draft/2020-12/schema")
            .put("type", "object").put("title", title)
        val properties = schema.putObject("properties")
        fields.forEach { (name, field) -> properties.set<ObjectNode>(name, field) }
        schema.putArray("required").apply { fields.forEach { add(it.first) } }
        return HalForm(
            links = mapOf("target" to mapOf("href" to target, "templated" to true)),
            method = "GET",
            contentType = null,
            schema = schema
        )
    }

    /** A bed's size: rows and columns are required, a raised bed's height is not. */
    private fun dimensionsSchema(): ObjectNode {
        val dimensions = JsonNodeFactory.instance.objectNode().put("type", "object")
        val properties = dimensions.putObject("properties")
        listOf(
            "rows" to "How many rows of cells", "columns" to "How many columns", "height" to "How tall the bed is"
        ).forEach { (name, description) ->
            properties.putObject(name).put("type", "integer").put("description", description)
        }
        dimensions.putArray("required").add("rows").add("columns")
        return dimensions
    }

    private fun letterField(letters: List<String>) = text().put("title", "Row").put("description", "The row's letter")
        .also { field ->
        field.putArray("enum").apply { letters.forEach(::add) }
    }

    private fun numberField(range: IntRange) = JsonNodeFactory.instance.objectNode()
        .put("type", "integer").put("title", "Column").put("description", "The column's number")
        .put("minimum", range.first).put("maximum", range.last)

    /**
     * The form that prepares a new bed: its name and size are the client's to choose, and so is its id
     * if the client wants one; left out, the server chooses it.
     */
    fun prepareBed(): HalForm {
        val schema = generator.generateSchema(PrepareBed::class.java)
        schema.remove(listOf("\$defs", "definitions"))
        val properties = schema.with("properties")
        properties.set<ObjectNode>("bedId", text().put("format", "uuid"))
        properties.set<ObjectNode>("dimensions", dimensionsSchema())
        schema.putArray("required").apply { listOf("name", "dimensions").forEach(::add) }
        FormWords.describe("prepare-bed", schema)
        return HalForm(
            links = mapOf("target" to mapOf("href" to "/api/beds")),
            method = "POST",
            contentType = MediaType.APPLICATION_JSON_VALUE,
            schema = schema
        )
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
        properties.set<ObjectNode>("location", text().put("description", FormWords.LOCATION_DESCRIPTION))
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
