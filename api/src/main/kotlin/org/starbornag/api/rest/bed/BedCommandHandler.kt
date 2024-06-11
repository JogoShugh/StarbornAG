package org.starbornag.api.rest.bed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.starbornag.api.domain.bed.BedRepository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RestController
class BedCommandHandler(
    private val bedCommandMapper: BedCommandMapper
) {
    companion object {
        val emitters = ConcurrentHashMap<UUID, SseEmitter>()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PostMapping("/api/beds/{bedId}/{action}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun handle(
        @PathVariable bedId: UUID,
        @PathVariable action: String,
        @RequestBody commandPayload: Any
    ): ResponseEntity<BedResourceWithCurrentState>  {
        try {
            val emitter = emitters.getOrPut(bedId) {
                SseEmitter()
            }
            val bed = BedRepository.getBed(bedId)
            val command = bedCommandMapper.convertCommand(action, commandPayload)
            scope.launch {
                bed?.execute(command, emitter) // Execute the command directly
            }
            val resource = BedResourceWithCurrentState.from(bed!!)
            val response = ResponseEntity.ok(resource)
            return response
        } catch(e: Exception) {
            println("Here is the exception: " + e.stackTraceToString())
            throw e
        }
    }

    @GetMapping("/api/beds/{bedId}/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(@PathVariable bedId: UUID): SseEmitter? {
        val emitter = emitters.getOrPut(bedId) {
            SseEmitter()
        }
        return emitter
    }
}