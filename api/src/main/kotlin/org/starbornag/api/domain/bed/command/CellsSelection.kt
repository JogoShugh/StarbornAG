package org.starbornag.api.domain.bed.command

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.starbornag.api.domain.bed.command.serialization.CellsSelectionDeserializer

@JsonDeserialize(using = CellsSelectionDeserializer::class)
data class CellsSelection(
    val row: Int? = null,
    val column: Int? = null,
    val cell: CellPosition? = null,
    val cells: CellPositionList? = null,
    val cellRange: CellRange? = null,
    val cellStart: CellPosition? = null,
    val cellEnd: CellPosition? = null
) {
    @get:JsonIgnore
    val isSingleCell get() = hasCell && !hasRow && !hasColumn && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange
    @get:JsonIgnore
    val isSingleColumn get() = hasColumn && !hasRow && !hasCell && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange
    @get:JsonIgnore
    val isSingleRow get() = hasRow && !hasColumn && !hasCell && !hasCells && !hasCellStart && !hasCellEnd && !hasCellRange
    @get:JsonIgnore
    val isCellList get() = hasCells && !hasColumn && !hasRow && !hasCell && !hasCellStart && !hasCellEnd && !hasCellRange
    @get:JsonIgnore
    private val isRangeByCellStartAndEnd get() = !hasCellRange && hasCellStart && hasCellEnd
    @get:JsonIgnore
    private val isRangeByCellRange get() = hasCellRange && !hasCellStart && !hasCellEnd

    suspend fun streamCellPositions(rowCount: Int, columnCount: Int) : Sequence<CellPosition> {
        if (isCellList) {
            return sequence {
                cells!!.forEach {
                    yield(it)
                }
            }
        }

        val (start, end) = when {
            isRangeByCellRange -> cellRange!!
            isRangeByCellStartAndEnd -> CellRange(cellStart!!, cellEnd!!)
            else -> when {
                isSingleCell -> CellRange(cell!!, cell)
                isSingleColumn -> CellRange(
                        CellPosition(1, column!!),
                        CellPosition(rowCount, column)
                    )
                isSingleRow -> CellRange(
                        CellPosition(row!!, 1),
                        CellPosition(row, columnCount)
                    )
                else -> {
                    TODO()
                }
            }
        }

        return sequence {
            for (row in start.row .. end.row) {
                for (col in start.column .. end.column) {
                    yield(CellPosition(row, col))
                }
            }
        }
     }

    private val hasRow get() = row != null
    private val hasColumn get() = column != null
    private val hasCell get() = cell != null
    private val hasCells get() = cells != null
    private val hasCellStart get() = cellStart != null
    private val hasCellEnd get() = cellEnd != null
    private val hasCellRange get() = cellRange != null
}