package org.starbornag.api.rest.bed

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.BedRepository
import java.util.*

@RestController
class BedCurrentStateQueryHandler {

    @GetMapping("/api/beds/{bedId}")
    suspend fun handle(@PathVariable bedId: UUID): ResponseEntity<BedResourceWithCurrentState> {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithCurrentState.from(bed!!)
        return ResponseEntity.ok(resource)
    }

    @GetMapping("/api/beds/{bedId}/negotiable", produces =
        [MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE])
    suspend fun negotiable(@PathVariable bedId: UUID, @RequestHeader("Accept") acceptHeader: MediaType?) : BedEvent {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithCurrentState.from(bed!!)
        val event = resource.rows[0].cells[0].events[0]
        return event
    }
}