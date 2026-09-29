package org.starbornag.api.sse

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import org.starbornag.api.application.bed.BedEventPublisher
import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedEvent

/** Adapter: announces stored events on the SSE bus the bed page subscribes to. */
class SseBedEventPublisher(private val sseEventBus: SseEventBus) : BedEventPublisher {
    override suspend fun publish(events: List<BedEvent>) {
        events.forEach { sseEventBus.handleEvent(SseEvent.of(sseEventName(it), it)) }
    }
}

/** The bed page swaps a cell's plant icon on "plants-<cell>" and appends care icons on "events-<cell>". */
fun sseEventName(event: BedEvent): String =
    when (event) {
        is BedCellPlanted -> "plants-${event.bedCellId}"
        else -> "events-${event.bedCellId}"
    }
