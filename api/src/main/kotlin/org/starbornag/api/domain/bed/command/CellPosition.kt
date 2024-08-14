package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.starbornag.api.domain.bed.command.serialization.CellPositionDeserializer

@JsonDeserialize(using = CellPositionDeserializer::class)
data class CellPosition(
    val row: Int,
    val column: Int) {
    companion object {
        fun isMatch(input: String) = cellRegex.matchEntire(input.trim()) != null
        // TODO: input validation checking
        // TODO: add an integer based one....
        fun of(rowIndexOrAlphaAlias: String, columnIndex: String) : CellPosition {
            val (row, column) = when {
                rowIndexOrAlphaAlias.matches(intRegex) -> {
                    // If the first part is a number, it's the row
                    rowIndexOrAlphaAlias.toInt() to columnIndex.toInt()
                }
                else -> {
                    // Otherwise, it's the column letters
                    rowIndexOrAlphaAlias.uppercase().map { it - 'A' + 1 }.sum() to columnIndex.toInt()
                }
            }
            // Adjust for 1-based indexing and handle 0 or negative input
            val adjustedRow = if (row <= 0) 1 else row
            val adjustedColumn = if (column <= 0) 1 else column

            return CellPosition(adjustedRow, adjustedColumn)
        }
        // TODO: more robust checking and throw meaningful error...
        fun of(input: String): CellPosition = fromString(input)!!

        fun fromString(input: String): CellPosition? {
            val matchResult = cellRegex.matchEntire(input.trim())
            if (matchResult != null) {
                val (rowOrColLetters, colStr) = matchResult.destructured
                val cellPosition = CellPosition.of(rowOrColLetters, colStr)
                return cellPosition
            }
            return null
        }
    }
}