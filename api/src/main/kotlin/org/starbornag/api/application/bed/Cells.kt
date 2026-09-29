package org.starbornag.api.application.bed

import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.Cell
import org.starbornag.api.domain.bed.cellsAt
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.Repository
import java.util.*

/** Use cases for cells. Each cell is one stream in the event store, identified by the cell id. */
class Cells(eventStore: EventStore, private val beds: Beds, private val publisher: BedEventPublisher) {
    private val repository = Repository<Cell?, BedEvent>(eventStore, Cell::class, { null }, Cell::evolve)

    /**
     * Records the command on every cell its location names, announces each cell's events once
     * they are stored, and returns all recorded events.
     *
     * The location is checked against the bed before anything is recorded. After that, each cell
     * is its own stream and its own transaction: this scales, but a failure part way leaves the
     * earlier cells recorded. The caller retries; atomicity across cells is deliberately not offered.
     *
     * @throws UnknownBed when the command's bed was never prepared.
     * @throws org.starbornag.api.domain.bed.LocationOutsideBed when the location leaves the bed.
     */
    suspend fun handle(command: CellCommand): List<BedEvent> {
        val bed = beds.find(command.bedId) ?: throw UnknownBed(command.bedId)
        return bed.cellsAt(command.location).flatMap { cellId ->
            val decide = Cell.decide(bed.id, cellId, command)
            var recorded = emptyList<BedEvent>()
            repository.handle(cellId) { cell -> decide(cell).also { recorded = it } }
            publisher.publish(recorded)
            recorded
        }
    }

    suspend fun find(cellId: UUID): Cell? = repository.find(cellId)?.state
}

class UnknownBed(val bedId: UUID) : NoSuchElementException("Bed $bedId has not been prepared")
