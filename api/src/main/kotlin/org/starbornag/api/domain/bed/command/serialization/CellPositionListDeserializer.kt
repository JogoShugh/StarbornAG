package org.starbornag.api.domain.bed.command.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellPositionList

class CellPositionListDeserializer : JsonDeserializer<CellPositionList?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CellPositionList? {
        val node: JsonNode = p.codec.readTree(p)

        return if (node.isTextual) {
            val cellPosition = CellPosition.fromString(node.asText())
            if (cellPosition != null) CellPositionList(cellPosition) else null
        } else if (node.isArray) {
            val cellPositions = node.mapNotNull { CellPosition.fromString(it.asText()) }
            if (cellPositions.isNotEmpty()) CellPositionList(*cellPositions.toTypedArray()) else null
        } else {
            TODO()
        }
    }
}