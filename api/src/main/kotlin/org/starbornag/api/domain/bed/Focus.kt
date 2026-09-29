package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellsSelection

/** A way to move the focus: one of eight compass directions, or out to a wider view. */
enum class Move(val word: String, private val direction: String?) {
    NORTHWEST("northwest", "nw"),
    NORTH("north", "n"),
    NORTHEAST("northeast", "ne"),
    WEST("west", "w"),
    EAST("east", "e"),
    SOUTHWEST("southwest", "sw"),
    SOUTH("south", "s"),
    SOUTHEAST("southeast", "se"),
    ZOOM_OUT("zoom-out", null);

    companion object {
        fun of(word: String): Move = entries.first { it.word == word }

        /** The move for a compass direction as CellPosition.neighbors names it ("nw", "n", …). */
        fun ofDirection(direction: String): Move = entries.first { it.direction == direction }
    }
}

/**
 * Where the gardener stands in a bed: on one cell, a row, a column or the whole bed.
 * Care given "here" applies to every cell the focus covers. Rows are letters and columns numbers,
 * so row 2, column 4 is B4.
 */
sealed interface Focus {
    /** The moves possible from here in a bed of [rows] × [columns] cells, and where each leads. */
    fun moves(rows: Int, columns: Int): Map<Move, Focus>

    /** The cells a command given here applies to; null means the whole bed. */
    val location: CellsSelection?

    /** A short name to show and say, such as "B4", "Row B", "Column 4" or "Whole bed". */
    val label: String

    /** The positions this focus covers, in row-major order. */
    fun cells(rows: Int, columns: Int): List<CellPosition> =
        location?.streamCellPositions(rows, columns)?.toList()
            ?: (1..rows).flatMap { row -> (1..columns).map { CellPosition(row, it) } }

    data class OnCell(val position: CellPosition) : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> =
            position.neighbors(rows, columns)
                .associate { (direction, next) -> Move.ofDirection(direction) to OnCell(next) } +
                (Move.ZOOM_OUT to OnRow(position.row))

        override val location get() = CellsSelection(cell = position)
        override val label get() = "${rowLetter(position.row)}${position.column}"
    }

    data class OnRow(val row: Int) : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = buildMap {
            if (row > 1) put(Move.NORTH, OnRow(row - 1))
            if (row < rows) put(Move.SOUTH, OnRow(row + 1))
            put(Move.ZOOM_OUT, OnBed)
        }

        override val location get() = CellsSelection(row = row)
        override val label get() = "Row ${rowLetter(row)}"
    }

    data class OnColumn(val column: Int) : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = buildMap {
            if (column > 1) put(Move.WEST, OnColumn(column - 1))
            if (column < columns) put(Move.EAST, OnColumn(column + 1))
            put(Move.ZOOM_OUT, OnBed)
        }

        override val location get() = CellsSelection(column = column)
        override val label get() = "Column $column"
    }

    data object OnBed : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = emptyMap()
        override val location: CellsSelection? get() = null
        override val label get() = "Whole bed"
    }
}

/** Row 1 is A, row 2 is B, and so on. */
fun rowLetter(row: Int): String = ('A' + (row - 1)).toString()
