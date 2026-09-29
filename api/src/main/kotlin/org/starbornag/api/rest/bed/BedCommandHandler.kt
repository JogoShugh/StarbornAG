package org.starbornag.api.rest.bed

import ch.rasc.sse.eventbus.SseEventBus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.starbornag.api.application.bed.BedCells
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import java.util.*

@RestController
class BedCommandHandler(
    private val bedCommandMapper: BedCommandMapper,
    private val sseEventBus: SseEventBus,
    private val bedCells: BedCells,
    private val resources: BedResources
) {

    /** Records a care command on the cells it names and answers with the bed's updated cells. */
    @PostMapping("/api/beds/{bedId}/{action}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun handle(
        @PathVariable bedId: UUID,
        @PathVariable action: String,
        @RequestBody commandPayload: Any
    ): ResponseEntity<BedResourceWithCurrentState> {
        val command = bedCommandMapper.convertCommand(action, commandPayload) as CellCommand
        bedCells.handle(command)
        return ResponseEntity.ok(resources.currentState(bedId))
    }

    /** Subscribes a client to the bed's cell announcements (see SseBedEventPublisher). */
    @GetMapping(
        "/api/beds/{bedId}/events",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE]
    )
    suspend fun events(
        @PathVariable bedId: UUID,
        @RequestParam clientId: UUID,
        @RequestHeader("Accept") acceptHeader: MediaType?
    ): ResponseEntity<SseEmitter> {
        val eventNames = resources.bed(bedId).rows.flatten().flatMap { listOf("events-$it", "plants-$it") }
        val mediaType = acceptHeader ?: MediaType.TEXT_PLAIN
        // createSseEmitter is a Java varargs method; the spread copies one short array per subscription.
        @Suppress("SpreadOperator")
        val emitter = sseEventBus.createSseEmitter(
            clientId.toString(), SUBSCRIPTION_TIMEOUT_MILLIS, mediaType, *eventNames.toTypedArray()
        )
        return ResponseEntity.ok(emitter)
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 120_000L
    }
}
