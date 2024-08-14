package org.starbornag.api.domain.bed.command.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellPositionList
import org.starbornag.api.domain.bed.command.CellRange
import org.starbornag.api.domain.bed.command.CellsSelection

class CellsSelectionDeserializer : JsonDeserializer<CellsSelection>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CellsSelection {
        val node: JsonNode = p.readValueAsTree()
        return if (node.isTextual) {
            val input = node.asText()
            // Test CellRange first, since it would also match for CellPosition
            if (CellRange.isMatch(input)) {
                CellsSelection(cellRange = CellRange.fromString(input))
            } else if (CellPosition.isMatch(input)) {
                CellsSelection(cell = CellPosition.fromString(input))
            } else {
                // TODO provide additional info here
                throw JsonMappingException(p, "Invalid JSON format for CellsSelection")
            }
        } else if (node.isObject) {
            // Manually deserialize other properties
            val row = if (node.hasNonNull("row")) node.get("row").asInt() else null
            val column = if (node.hasNonNull("column")) node.get("column").asInt() else null
            val cell = node.get("cell")?.let {
                if (it.isObject) {
                    val cellPosition = p.codec.treeToValue(it, CellPosition::class.java)
                    cellPosition
                } else if (it.isTextual) {
                    CellPosition.fromString(it.asText())
                } else {
                    TODO()
                }
            }
            val cellRange = node.get("cellRange")?.let {
                p.codec.treeToValue(it, CellRange::class.java)
            }
            val cells = node.get("cells")?.let {
                val cellPositions = it.mapNotNull { CellPosition.fromString(it.asText()) }
                val cellPositionList = if (cellPositions.isNotEmpty()) CellPositionList(*cellPositions.toTypedArray()) else null
                cellPositionList
            }
            val cellStart = node.get("cellStart")?.let { CellPosition.fromString(it.asText()) }
            val cellEnd = node.get("cellEnd")?.let { CellPosition.fromString(it.asText()) }

            CellsSelection(row, column, cell, cells, cellRange, cellStart, cellEnd)
        } else {
            // Handle other unexpected cases or throw an exception
            throw JsonMappingException(p, "Invalid JSON format for CellsSelection")
        }
    }
}