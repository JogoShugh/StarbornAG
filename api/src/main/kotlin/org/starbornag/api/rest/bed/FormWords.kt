package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * What each form and each field is called and what it means, as the forms tell agents (JSON Schema
 * "title" and "description") and people. Anything measured says its unit.
 */
object FormWords {
    const val LOCATION_DESCRIPTION =
        "Cells as spoken: A1, B2 to C4, A1 A3 B5, or 3:2 (letter = row, number = column). Omit for the whole bed."

    private val titles = mapOf(
        "plant-seedling" to "Plant a seedling",
        "water-cells" to "Water",
        "fertilize-cells" to "Feed",
        "mulch-cells" to "Mulch",
        "harvest-crop" to "Harvest",
        "prepare-bed" to "Prepare a bed"
    )

    private val fields = mapOf(
        "bedId" to ("Bed" to "The bed, fixed by the address the form came from"),
        "location" to ("Cells" to LOCATION_DESCRIPTION),
        "started" to (
            "When" to "When the care happened, as an ISO 8601 date and time; left out, the moment it arrives"
        ),
        "plantType" to ("Plant" to "What grows there, such as tomato"),
        "plantCultivar" to ("Variety" to "The cultivar, such as Dark Galaxy"),
        "fertilizer" to ("Fertilizer" to "Which fertilizer, such as fish emulsion"),
        "material" to ("Mulch" to "Which mulch, such as straw"),
        "quantity" to ("Count" to "How many were picked"),
        "weight" to ("Weight" to "How much the harvest weighed, in kilograms"),
        "name" to ("Name" to "What the bed is called, such as Earth"),
        "dimensions" to ("Size" to "How many rows and columns the bed has"),
        "cellBlockSize" to ("Cell width" to "How many columns one cell spans; each row gets columns ÷ this many cells")
    )

    private val volumes = mapOf(
        "water-cells" to "How much water was given, in liters",
        "fertilize-cells" to "How much fertilizer was given, in liters",
        "mulch-cells" to "How much mulch was spread, in liters"
    )

    private val ownBedIds = mapOf("prepare-bed" to "The new bed's id; left out, the server chooses one")

    /** Gives the form [formId]'s [schema] its title, and each of its fields a title and a description. */
    fun describe(formId: String, schema: ObjectNode): ObjectNode {
        titles[formId]?.let { schema.put("title", it) }
        schema.with("properties").properties().forEach { (name, field) ->
            val (title, description) = fields[name] ?: return@forEach
            val meaning = if (name == "bedId") ownBedIds[formId] ?: description else description
            (field as ObjectNode).put("title", title).put("description", meaning)
        }
        volumes[formId]?.let { volume ->
            (schema.with("properties").get("volume") as? ObjectNode)?.put("title", "Volume")?.put("description", volume)
        }
        return schema
    }
}
