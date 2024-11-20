package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import kotlinx.coroutines.CoroutineScope
import org.starbornag.api.LogTimer.logNow
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand

interface IBedEventBus {
    suspend fun publishEvent(command: CellCommand,
                     event: BedEvent)
}

class BedEventBus(private val sseEventBus: SseEventBus, private val applicationScope: CoroutineScope) : IBedEventBus {
    override suspend fun publishEvent(command: CellCommand, event: BedEvent) {
        val eventName = when(event) {
            is BedCellPlanted -> "plants-${event.bedCellId}"
            else -> "events-${event.bedCellId}"
        }
        logNow("publishEvent")
        sseEventBus.handleEvent(SseEvent.of(eventName, event))
    }
}