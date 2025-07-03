package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.starbornag.api.data.Event
import org.starbornag.api.data.EventRepository
import java.util.*

@RestController
class EventTestController(
    private val eventRepository: EventRepository,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/api/es/{streamId}/{expectedVersion}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun handle(
        @PathVariable streamId: UUID,
        @PathVariable expectedVersion: Long,
        @RequestBody eventPayload: Any
    ): ResponseEntity<Boolean>  {
        try {
            val eventData = objectMapper.writeValueAsString(eventPayload)
            val event = Event(UUID.randomUUID(), eventData, streamId, "TestHappened", expectedVersion)
            val result = eventRepository.appendEvent(event.id, event.data, event.type, event.streamId, event.type, event.version)
            val response = ResponseEntity.ok(result)
            return response
        } catch(e: Exception) {
            println("Here is the exception: " + e.stackTraceToString())
            throw e
        }
    }

    @GetMapping("/api/es/{streamId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getEvents(
        @PathVariable streamId: UUID
    ) : ResponseEntity<Flow<Event>> = ResponseEntity.ok(eventRepository.getEventsByStreamId(streamId))
}