package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.starbornag.api.domain.bed.command.serialization.CellPositionListDeserializer

@JsonDeserialize(using = CellPositionListDeserializer::class)
class CellPositionList(vararg initialValues: CellPosition) : List<CellPosition> by initialValues.toList() {
    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String) : CellPositionList? {
            val parts = value.split(Regex("""\s*[,\s]+\s*""")) // Split on comma or whitespace
            val cells = parts
                .mapNotNull { part ->  // Use mapNotNull to filter out nulls
                    cellRegex.matchEntire(part.trim())?.let { matchResult ->
                        CellPosition.of(matchResult.value)
                    }
                }
                .toList() // Convert to a List
            return if (cells.isNotEmpty()) CellPositionList(*cells.toTypedArray()) else null
        }
    }
}