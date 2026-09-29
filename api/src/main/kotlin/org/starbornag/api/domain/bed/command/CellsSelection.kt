package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class CellsSelection(
    val row: Int? = null,
    val rows: List<Int>? = null,
    val column: Int? = null,
    val columns: List<Int>? = null,
    val cell: CellPosition? = null,
    val cells: CellPositionList? = null,
    val cellRange: CellRange? = null,
    val cellStart: CellPosition? = null,
    val cellEnd: CellPosition? = null) {

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(value: String): CellsSelection {
            // Test CellRange first, since it would also match for CellPosition
            return if (CellRange.isMatch(value)) {
                CellsSelection(cellRange = CellRange.fromString(value))
            } else if (CellPositionList.isMatch(value)) {
                CellsSelection(cells = CellPositionList.fromString(value))
            } else if (CellPosition.isMatch(value)) {
                CellsSelection(cell = CellPosition.fromString(value))
            } else {
                TODO()
            }
        }
    }

    @get:JsonIgnore
    val isSingleCell get() = hasCell && !hasRow && !hasColumn && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange

    @get:JsonIgnore
    val isSingleColumn get() = hasColumn && !hasRow && !hasCell && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange

    @get:JsonIgnore
    val isSingleRow get() = hasRow && !hasColumn && !hasCell && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange

    @get:JsonIgnore
    private val isRangeByCellStartAndEnd get() = !hasCellRange && hasCellStart && hasCellEnd

    @get:JsonIgnore
    private val isRangeByCellRange get() = hasCellRange && !hasCellStart && !hasCellEnd

    private val hasRow get() = row != null
    private val hasRows get() = rows != null
    private val hasColumn get() = column != null
    private val hasColumns get() = columns != null
    private val hasCell get() = cell != null
    private val hasCells get() = cells != null
    private val hasCellStart get() = cellStart != null
    private val hasCellEnd get() = cellEnd != null
    private val hasCellRange get() = cellRange != null

    /**
     * Every position the selection names, in this order: listed cells, whole rows, whole columns,
     * the single cell, then the range (a cell range, a start and end cell, one column or one row).
     */
    fun streamCellPositions(rowCount: Int, columnCount: Int): Sequence<CellPosition> =
        listedCells() + wholeRows(columnCount) + wholeColumns(rowCount) + singleCell() +
            rangeCells(range(rowCount, columnCount))

    private fun listedCells(): Sequence<CellPosition> =
        if (hasCells) cells.orEmpty().asSequence() else emptySequence()

    private fun wholeRows(columnCount: Int): Sequence<CellPosition> =
        if (!hasRows) emptySequence()
        else rows.orEmpty().asSequence().flatMap { row -> (1..columnCount).asSequence().map { CellPosition(row, it) } }

    private fun wholeColumns(rowCount: Int): Sequence<CellPosition> =
        if (!hasColumns) emptySequence()
        else columns.orEmpty().asSequence().flatMap { col -> (1..rowCount).asSequence().map { CellPosition(it, col) } }

    private fun singleCell(): Sequence<CellPosition> =
        if (hasCell) sequenceOf(cell!!) else emptySequence()

    private fun range(rowCount: Int, columnCount: Int): CellRange? =
        when {
            isRangeByCellRange -> cellRange!!
            isRangeByCellStartAndEnd -> CellRange(cellStart!!, cellEnd!!)
            hasColumn -> CellRange(CellPosition(1, column!!), CellPosition(rowCount, column))
            hasRow -> CellRange(CellPosition(row!!, 1), CellPosition(row, columnCount))
            else -> null
        }

    private fun rangeCells(range: CellRange?): Sequence<CellPosition> {
        val (start, end) = range ?: return emptySequence()
        return (start.row..end.row).asSequence().flatMap { row ->
            (start.column..end.column).asSequence().map { col -> CellPosition(row, col) }
        }
    }
}