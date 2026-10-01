package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*

/** JSON Schema properties the forms share: the bed a form belongs to, a bed's size, rows and columns. */
object FormFields {
    private fun field(type: String): ObjectNode = JsonNodeFactory.instance.objectNode().put("type", type)

    /**
     * The bed a form belongs to, fixed the JSON Schema way: "const" (its only valid value), "default"
     * (the value to start from) and "readOnly" (managed by the server alone).
     */
    fun bed(bedId: UUID): ObjectNode = field("string").put("format", "uuid")
        .put("const", bedId.toString()).put("default", bedId.toString()).put("readOnly", true)
        .put("title", "Bed").put("description", "The bed, fixed by where the form came from")

    /** A bed's size: rows and columns are required, a raised bed's height is not. */
    fun dimensions(): ObjectNode {
        val dimensions = field("object")
        val properties = dimensions.putObject("properties")
        listOf(
            "rows" to "How many rows of cells", "columns" to "How many columns", "height" to "How tall the bed is"
        ).forEach { (name, description) ->
            properties.set<ObjectNode>(name, field("integer").put("description", description))
        }
        dimensions.putArray("required").add("rows").add("columns")
        return dimensions
    }

    /** A row, by letter: only the [letters] that exist. */
    fun letters(letters: List<String>): ObjectNode =
        field("string").put("title", "Row").put("description", "The row's letter")
            .also { row -> row.putArray("enum").apply { letters.forEach(::add) } }

    /** A column, by number: only the numbers in [range]. */
    fun numbers(range: IntRange): ObjectNode =
        field("integer").put("title", "Column").put("description", "The column's number")
            .put("minimum", range.first).put("maximum", range.last)
}
