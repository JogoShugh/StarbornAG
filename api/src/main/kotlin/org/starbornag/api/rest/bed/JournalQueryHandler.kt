package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/** Serves the journal of a focus for agents, folded by cell, row, column or time. */
@RestController
class JournalQueryHandler(private val resources: BedResources) {

    @GetMapping("/api/beds/{bedId}/journal")
    suspend fun journal(
        @PathVariable bedId: UUID,
        @RequestParam(defaultValue = "bed") focus: String,
        @RequestParam(defaultValue = "cell") by: String
    ): ResponseEntity<JournalResource> = resources.ok(resources.journal(bedId, focus, by))
}
