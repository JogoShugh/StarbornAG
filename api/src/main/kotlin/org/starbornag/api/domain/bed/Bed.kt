package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
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

class BedAlreadyExists(val bedId: UUID) : IllegalStateException("Bed $bedId has already been prepared")
