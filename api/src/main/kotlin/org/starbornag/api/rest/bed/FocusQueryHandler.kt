package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

/** Serves a focus: "cell/B4", "row/B", "column/4", or the whole bed. */
@RestController
class FocusQueryHandler(private val resources: BedResources) {

    @GetMapping("/api/beds/{bedId}/focus/bed")
    suspend fun wholeBed(@PathVariable bedId: UUID): ResponseEntity<FocusResource> =
        resources.ok(resources.focus(bedId, "bed"))

    @GetMapping("/api/beds/{bedId}/focus/{scope}/{ref}")
    suspend fun focus(
        @PathVariable bedId: UUID,
        @PathVariable scope: String,
        @PathVariable ref: String
    ): ResponseEntity<FocusResource> = resources.ok(resources.focus(bedId, "$scope/$ref"))
}
