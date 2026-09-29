package org.starbornag.api.domain.bed

import org.starbornag.eventstore.EventStore
import java.util.*

/** Loads and stores a bed cell's events. Each cell is one stream, identified by the cell id. */
class BedCellStateRepository(private val eventStore: EventStore) {

    suspend fun fetch(cellId: UUID): BedCellAggregateState {
        val events = eventStore.getEvents(cellId).filterIsInstance<BedEvent>()
        return BedCellAggregateState.fromEvents(events.asSequence())
    }

    suspend fun append(cellId: UUID, events: List<BedEvent>) {
        eventStore.appendEvents(BedCellAggregate::class, cellId, events)
    }
}
