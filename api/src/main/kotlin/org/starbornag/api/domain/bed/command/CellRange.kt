package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.starbornag.api.domain.bed.command.serialization.CellRangeDeserializer

@JsonDeserialize(using = CellRangeDeserializer::class)
data class CellRange(
    val cellStart: CellPosition,
    val cellEnd: CellPosition
) {
    companion object {
        fun isMatch(input: String) = rangeRegex.matchEntire(input.trim()) != null
        fun of(cellStartRow: Int, cellStartColumn: Int, cellEndRow: Int, cellEndColumn: Int) =
            CellRange(CellPosition(cellStartRow, cellStartColumn), CellPosition(cellEndRow, cellEndColumn))
        fun of(cellStart: String, cellEnd: String) =
            // TODO: should I not allow the !! here?
            CellRange(CellPosition.fromString(cellStart)!!, CellPosition.fromString(cellEnd)!!)
        fun fromString(input: String): CellRange? {
            val matchResult = rangeRegex.matchEntire(input)
            return when {
                matchResult != null -> {
                    val (row1, col1, row2, col2) = matchResult.destructured
                    val cellStart = CellPosition.fromString(row1 + col1)!!
                    val cellEnd = CellPosition.fromString(row2 + col2)!!
                    CellRange(
                        cellStart,
                        cellEnd
                    )
                }
                else -> null
            }
        }
    }
}