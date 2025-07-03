package org.starbornag.api.rest.bed

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

data class AlgoProgress(
    val array: List<Int>
)

@RestController
class AlgoTestController(
    private val sseEventBus: SseEventBus,
    private val applicationScope: CoroutineScope
) {

    private suspend fun insertionSort(arr: IntArray, delayMs: Long, sseEmitter: SseEmitter) {
        if (arr.size < 2) return
        for(i in 1 until arr.size) {
            delay(delayMs)
            val elem = arr[i]
            var j = i - 1
            while(j >= 0 && arr[j] > elem) {
                arr[j + 1] = arr[j]
                publishAlgoProgress(sseEmitter, arr.toList())
                j--
                delay(delayMs)
            }
            arr[j + 1] = elem
            publishAlgoProgress(sseEmitter, arr.toList())
        }
    }

    private fun publishAlgoProgress(sseEmitter: SseEmitter, array: List<Int>) =
        sseEventBus.handleEvent(SseEvent.of("algo-progress", AlgoProgress(array)))

    @PostMapping(
        "/api/algo/insertionSort/{clientId}", consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun insertionSortHandler(
        @PathVariable clientId: UUID,
        @RequestParam delayMs: Long = 250,
        @RequestBody array: IntArray,
        @RequestHeader("Accept") acceptHeader: MediaType?
    ): ResponseEntity<SseEmitter> {
        try {
            val mediaType = acceptHeader ?: MediaType.APPLICATION_JSON
            val eventNames = arrayOf("algo-progress")
            val emitter = sseEventBus.createSseEmitter(
                clientId.toString(),
                120_000L,
                mediaType,
                *eventNames
            )
            applicationScope.launch {
                insertionSort(array, delayMs, emitter)
            }
            return ResponseEntity.ok(emitter)
        } catch (e: Exception) {
            println("Here is the exception: " + e.stackTraceToString())
            throw e
        }
    }
}