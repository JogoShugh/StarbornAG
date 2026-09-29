package org.starbornag.api.application.bed

import org.starbornag.api.domain.bed.BedCell
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.cellsAt
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.Repository
import java.util.*

/** Use cases for bed cells. Each cell is one stream in the event store, identified by the cell id. */
class BedCells(
    private val eventStore: EventStore,
    private val beds: Beds,
    private val publisher: BedEventPublisher
) {
    private val repository = Repository<BedCell?, BedEvent>(eventStore, BedCell::class, { null }, BedCell::evolve)

    /**
     * Records the command on every cell its location names, announces each cell's events once
     * they are stored, and returns all recorded events.
     *
     * The location and the soil rules (see BedCell.targets) are checked before anything is recorded,
     * so a planting into an occupied cell records nothing. After that, each cell
     * is its own stream and its own transaction: this scales, but a failure part way leaves the
     * earlier cells recorded. The caller retries; atomicity across cells is deliberately not offered.
     *
     * @throws UnknownBed when the command's bed was never prepared.
     * @throws org.starbornag.api.domain.bed.LocationOutsideBed when the location leaves the bed.
     */
    suspend fun handle(command: CellCommand): List<BedEvent> {
        val bed = beds.find(command.bedId) ?: throw UnknownBed(command.bedId)
        val named = bed.cellsAt(command.location).map { find(it) ?: BedCell(it, bed.id) }
        return BedCell.targets(command, named).map { it.id }.flatMap { cellId ->
            val decide = BedCell.decide(bed.id, cellId, command)
            var recorded = emptyList<BedEvent>()
            repository.handle(cellId) { cell -> decide(cell).also { recorded = it } }
            publisher.publish(recorded)
            recorded
        }
    }

    suspend fun find(cellId: UUID): BedCell? = repository.find(cellId)?.state

    /** The cell's current state and its full history. A cell nothing has happened to yet has neither. */
    suspend fun load(bedId: UUID, cellId: UUID): LoadedBedCell {
        val history = eventStore.getEvents(cellId).filterIsInstance<BedEvent>()
        val state = history.fold(BedCell(cellId, bedId)) { cell, event -> BedCell.evolve(cell, event) }
        return LoadedBedCell(state, history)
    }
}

data class LoadedBedCell(val state: BedCell, val history: List<BedEvent>)

class UnknownBed(val bedId: UUID) : NoSuchElementException("Bed $bedId has not been prepared")
