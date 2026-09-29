package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import org.starbornag.api.LogTimer.logNow
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.eventstore.EventStore

interface IBedEventBus {
    suspend fun publishEvent(command: CellCommand,
                     event: BedEvent)
    suspend fun storeEvent(event: BedEvent)
}

class BedEventBus(private val sseEventBus: SseEventBus,
                  private val eventStore: EventStore) : IBedEventBus {
    override suspend fun publishEvent(command: CellCommand, event: BedEvent) {
        val eventName = when(event) {
            is BedCellPlanted -> "plants-${event.bedCellId}"
            else -> "events-${event.bedCellId}"
        }
        logNow("publishEvent")
        sseEventBus.handleEvent(SseEvent.of(eventName, event))
    }

    // Each cell is one stream. The in-memory aggregate tracks no version, so any version is accepted.
    override suspend fun storeEvent(event: BedEvent) {
        eventStore.appendEvents(BedCellAggregate::class, event.bedCellId, listOf(event))
    }

}