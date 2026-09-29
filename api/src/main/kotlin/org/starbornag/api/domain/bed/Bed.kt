package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellsSelection
import org.starbornag.api.domain.bed.command.Dimensions
import java.util.*

/**
 * A prepared bed: its size and the id of each square-foot cell, row by row.
 * State is rebuilt from the bed's stream with [evolve]; commands are decided with [prepare].
 */
data class Bed(
    val id: UUID,
    val name: String,
    val dimensions: Dimensions,
    val cellBlockSize: Int,
    val rows: List<List<UUID>>
) {
    sealed interface Event {
        data class BedPrepared(
            val bedId: UUID,
            val name: String,
            val dimensions: Dimensions,
            val cellBlockSize: Int,
            val rows: List<List<UUID>>
        ) : Event
    }

    companion object {
        // BedPrepared is the only bed event so far, so the previous state is not needed yet.
        @Suppress("UnusedParameter")
        fun evolve(bed: Bed?, event: Event): Bed? =
            when (event) {
                is Event.BedPrepared -> Bed(event.bedId, event.name, event.dimensions, event.cellBlockSize, event.rows)
            }

        /** Lays out one cell per block of columns in every row, each with a new id. */
        fun prepare(command: PrepareBed, newCellId: () -> UUID = UUID::randomUUID): (Bed?) -> List<Event> = { bed ->
            if (bed != null) throw BedAlreadyExists(command.bedId)
            val cellsPerRow = command.dimensions.columns / command.cellBlockSize
            val rows = List(command.dimensions.rows) { List(cellsPerRow) { newCellId() } }
            listOf(Event.BedPrepared(command.bedId, command.name, command.dimensions, command.cellBlockSize, rows))
        }
    }
}

/**
 * The ids of the cells a location names, in row-major order, or of every cell when there is
 * no location. Fails as a whole if any named cell lies outside the bed.
 */
fun Bed.cellsAt(location: CellsSelection?): List<UUID> {
    val rowCount = rows.size
    val columnCount = rows.firstOrNull()?.size ?: 0
    val positions = location?.streamCellPositions(rowCount, columnCount)?.distinct()?.toList()
        ?: return rows.flatten()
    positions.firstOrNull { it.row !in 1..rowCount || it.column !in 1..columnCount }
        ?.let { throw LocationOutsideBed(id, it) }
    return positions.map { rows[it.row - 1][it.column - 1] }
}

/** The 1-based row and column of a cell, or null when the cell is not part of this bed. */
fun Bed.positionOf(cellId: UUID): CellPosition? =
    rows.withIndex().firstNotNullOfOrNull { (rowIndex, row) ->
        row.indexOf(cellId).takeIf { it >= 0 }?.let { CellPosition(rowIndex + 1, it + 1) }
    }

class BedAlreadyExists(val bedId: UUID) : IllegalStateException("Bed $bedId has already been prepared")

class LocationOutsideBed(val bedId: UUID, val position: CellPosition) :
    IllegalArgumentException("Cell ${position.row}:${position.column} is outside bed $bedId")
