package org.starbornag.api.domain.bed

import org.springframework.stereotype.Component
import java.util.*

@Component
class BedCellStateRepository {
    private val eventStoreDBRepository = EventStoreDBRepository(BedEvent::class.java)

    companion object {
        private const val STREAM_PREFIX = "cellAggregate"
    }

    fun fetch(bedId: UUID, cellId: UUID) : BedCellAggregateState {
        val streamName = getStreamName(bedId, cellId)
        val events = eventStoreDBRepository.fetch(streamName)
        val state = BedCellAggregateState.fromEvents(events)
        return state
    }

    fun append(bedId: UUID, cellId: UUID, events: Iterable<BedEvent>) {
        val streamName = getStreamName(bedId, cellId)
        eventStoreDBRepository.append(streamName, events, 1)
    }

    private fun getStreamName(bedId: UUID, cellId: UUID) = "${STREAM_PREFIX}-${bedId}-${cellId}"
}