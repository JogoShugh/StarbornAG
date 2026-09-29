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

    /** The focus's address: "cell/B4", "row/B", "column/4" or "bed". */
    val path: String

    /**
     * The location as it is spoken and written in commands, such as "B4" or "B1 to B8";
     * null for the whole bed, which needs no location.
     */
    fun spokenLocation(rows: Int, columns: Int): String?

    /** Whether this focus lies inside a bed of [rows] × [columns] cells. */
    fun fits(rows: Int, columns: Int): Boolean

    companion object {
        /**
         * The focus at an address such as "cell/B4", "row/B", "column/4" or "bed".
         *
         * @throws FocusOutsideBed when the address is malformed or leaves the bed.
         */
        fun fromPath(path: String, rows: Int, columns: Int): Focus {
            val (scope, ref) = path.split("/").let { it[0] to it.getOrNull(1) }
            val focus = runCatching {
                when (scope) {
                    "cell" -> OnCell(CellPosition.of(ref!!))
                    "row" -> OnRow(rowNumber(ref!!))
                    "column" -> OnColumn(ref!!.toInt())
                    "bed" -> OnBed
                    else -> null
                }
            }.getOrNull()
            return focus?.takeIf { it.fits(rows, columns) } ?: throw FocusOutsideBed(path)
        }
    }

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
        override val path get() = "cell/$label"
        override fun spokenLocation(rows: Int, columns: Int) = label
        override fun fits(rows: Int, columns: Int) = position.row in 1..rows && position.column in 1..columns
    }

    data class OnRow(val row: Int) : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = buildMap {
            if (row > 1) put(Move.NORTH, OnRow(row - 1))
            if (row < rows) put(Move.SOUTH, OnRow(row + 1))
            put(Move.ZOOM_OUT, OnBed)
        }

        override val location get() = CellsSelection(row = row)
        override val label get() = "Row ${rowLetter(row)}"
        override val path get() = "row/${rowLetter(row)}"
        override fun spokenLocation(rows: Int, columns: Int) = "${rowLetter(row)}1 to ${rowLetter(row)}$columns"
        override fun fits(rows: Int, columns: Int) = row in 1..rows
    }

    data class OnColumn(val column: Int) : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = buildMap {
            if (column > 1) put(Move.WEST, OnColumn(column - 1))
            if (column < columns) put(Move.EAST, OnColumn(column + 1))
            put(Move.ZOOM_OUT, OnBed)
        }

        override val location get() = CellsSelection(column = column)
        override val label get() = "Column $column"
        override val path get() = "column/$column"
        override fun spokenLocation(rows: Int, columns: Int) = "A$column to ${rowLetter(rows)}$column"
        override fun fits(rows: Int, columns: Int) = column in 1..columns
    }

    data object OnBed : Focus {
        override fun moves(rows: Int, columns: Int): Map<Move, Focus> = emptyMap()
        override val location: CellsSelection? get() = null
        override val label get() = "Whole bed"
        override val path get() = "bed"
        override fun spokenLocation(rows: Int, columns: Int): String? = null
        override fun fits(rows: Int, columns: Int) = true
    }
}

/** Row 1 is A, row 2 is B, and so on. */
fun rowLetter(row: Int): String = ('A' + (row - 1)).toString()

/** Row A is 1, row B is 2, and so on; a number is taken as is. */
fun rowNumber(ref: String): Int = ref.toIntOrNull() ?: (ref.single().uppercaseChar() - 'A' + 1)

class FocusOutsideBed(path: String) : NoSuchElementException("No focus '$path' in this bed")
