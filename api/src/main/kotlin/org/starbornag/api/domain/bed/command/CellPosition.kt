package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.annotation.JsonCreator

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

        @JvmStatic
        @JsonCreator
        fun fromString(value: String): CellPosition? {
            val matchResult = cellRegex.matchEntire(value.trim())
            if (matchResult != null) {
                val (rowOrColLetters, colStr) = matchResult.destructured
                val cellPosition = CellPosition.of(rowOrColLetters, colStr)
                return cellPosition
            }
            return null
        }

        private val offsets = listOf(
            "nw" to (-1 to -1),
            "n" to (-1 to 0),
            "ne" to (-1 to 1),
            "w" to (0 to -1),
            "e" to (0 to 1),
            "sw" to (1 to -1),
            "s" to (1 to 0),
            "se" to (1 to 1)
        )
    }

    fun neighbors(rows: Int, cols: Int) = offsets.mapNotNull { (direction, offsets) ->
        val (rowOffset, colOffset) = offsets
        val newRow = row + rowOffset
        val newCol = column + colOffset
        if (newRow in 1 .. rows && newCol in 1 .. cols)
            direction to CellPosition(newRow, newCol) else null
    }

}

fun List<Pair<String, CellPosition>>.hasDirection(direction: String) =
    this.any { (directionName, _ ) -> directionName == direction }