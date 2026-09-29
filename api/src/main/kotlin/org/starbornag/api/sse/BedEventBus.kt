package org.starbornag.api.sse

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import org.starbornag.api.LogTimer.logNow
import org.starbornag.api.domain.bed.BedCellAggregate
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.IBedEventBus
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.eventstore.EventStore

/**
 * Legacy adapter for the in-memory BedAggregate path, still used by the REST and NLP handlers.
 * Replaced by the Cells use case with SseBedEventPublisher once those handlers move over.
 */
class BedEventBus(private val sseEventBus: SseEventBus,
                  private val eventStore: EventStore) : IBedEventBus {
    override suspend fun publishEvent(command: CellCommand, event: BedEvent) {
        logNow("publishEvent")
        sseEventBus.handleEvent(SseEvent.of(sseEventName(event), event))
    }

    // Each cell is one stream. The in-memory aggregate tracks no version, so any version is accepted.
    override suspend fun storeEvent(event: BedEvent) {
        eventStore.appendEvents(BedCellAggregate::class, event.bedCellId, listOf(event))
    }
}
