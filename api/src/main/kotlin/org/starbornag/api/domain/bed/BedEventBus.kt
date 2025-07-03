package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import org.starbornag.api.LogTimer.logNow
import org.starbornag.api.data.Event
import org.starbornag.api.data.EventRepository
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import java.util.*

interface IBedEventBus {
    suspend fun publishEvent(command: CellCommand,
                     event: BedEvent)
    suspend fun storeEvent(event: BedEvent)
}

class BedEventBus(private val sseEventBus: SseEventBus,
                  private val eventRepository: EventRepository,
                  private val objectMapper: ObjectMapper,
                  private val applicationScope: CoroutineScope) : IBedEventBus {
    override suspend fun publishEvent(command: CellCommand, event: BedEvent) {
        val eventName = when(event) {
            is BedCellPlanted -> "plants-${event.bedCellId}"
            else -> "events-${event.bedCellId}"
        }
        logNow("publishEvent")
        sseEventBus.handleEvent(SseEvent.of(eventName, event))
    }

    override suspend fun storeEvent(event: BedEvent) {
        val type = getEventType(event) ?: "Event"

        val data = objectMapper.writeValueAsString(event)

        val dataEvent = Event(UUID.randomUUID(), data, event.bedCellId, type, 0)
        eventRepository.appendEvent(dataEvent.id, dataEvent.data, dataEvent.type, dataEvent.streamId, dataEvent.type, dataEvent.version)
    }

    private fun getEventType(event: Any) =
        event::class.java.getAnnotation(JsonTypeName::class.java)?.value

}