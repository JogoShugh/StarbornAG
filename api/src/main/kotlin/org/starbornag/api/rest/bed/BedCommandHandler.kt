package org.starbornag.api.rest.bed

import ch.rasc.sse.eventbus.SseEventBus
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.starbornag.api.data.EventRepository
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedEventBus
import org.starbornag.api.domain.bed.BedRepository
import java.util.*

@RestController
class BedCommandHandler(
    private val bedCommandMapper: BedCommandMapper,
    private val sseEventBus: SseEventBus,
    private val eventRepository: EventRepository,
    private val objectMapper: ObjectMapper,
    private val applicationScope: CoroutineScope
) {

    @PostMapping("/api/beds/{bedId}/{action}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun handle(
        @PathVariable bedId: UUID,
        @PathVariable action: String,
        @RequestBody commandPayload: Any
    ): ResponseEntity<BedResourceWithCurrentState>  {
        try {
            val bed = BedRepository.getBed(bedId)
            val bedEventBus = BedEventBus(sseEventBus, eventRepository, objectMapper, applicationScope)
            val command = bedCommandMapper.convertCommand(action, commandPayload)
            bed?.execute(command, bedEventBus) // Execute the command directly
            val resource = BedResourceWithCurrentState.from(bed!!)
            val response = ResponseEntity.ok(resource)
            return response
        } catch(e: Exception) {
            println("Here is the exception: " + e.stackTraceToString())
            throw e
        }
    }

@GetMapping("/api/beds/{bedId}/events", produces =
    [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE])
fun events(@PathVariable bedId: UUID,
           @RequestParam clientId: UUID,
           @RequestHeader("Accept") acceptHeader: MediaType?
): ResponseEntity<SseEmitter> {
            val bed = BedRepository.getBed(bedId)
    val eventNames = getEventNamesFromBedCells(bed)
    val mediaType = acceptHeader ?: MediaType.TEXT_PLAIN
    return ResponseEntity.ok(sseEventBus.createSseEmitter(
            clientId.toString(),
        120_000L,
            mediaType,
            *eventNames.toTypedArray()
        )
    )
}

private fun getEventNamesFromBedCells(bed: BedAggregate?) =
    bed!!.rows.flatMap { row ->
        row.cells.flatMap { cell ->
            listOf("events-$cell", "plants-$cell")
        }
    }
}