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

    suspend fun streamCellPositions(rowCount: Int, columnCount: Int): Sequence<CellPosition> {
        return sequence {
            if (hasCells) {
                cells?.forEach {
                    yield(it)
                }
            }

            if (hasRows) {
                rows?.forEach { row ->
                    for (col in 1..columnCount)
                        yield(CellPosition(row, col))
                }
            }

            if (hasColumns) {
                columns?.forEach { column ->
                    for (row in 1..rowCount)
                        yield(CellPosition(row, column))
                }
            }

            if (hasCell) {
                yield(cell!!)
            }

            val range = when {
                isRangeByCellRange -> cellRange!!
                isRangeByCellStartAndEnd -> CellRange(cellStart!!, cellEnd!!)
                else -> when {
                    hasColumn -> CellRange(
                        CellPosition(1, column!!),
                        CellPosition(rowCount, column)
                    )
                    hasRow -> CellRange(
                        CellPosition(row!!, 1),
                        CellPosition(row, columnCount)
                    )

                    else -> {
                        null
                    }
                }
            }

            range?.let {
                val (start, end) = range
                for (row in start.row..end.row) {
                    for (col in start.column..end.column) {
                        yield(CellPosition(row, col))
                    }
                }
            }
        }
    }
}