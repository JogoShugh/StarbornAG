package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class BedHistoryQueryHandler(private val resources: BedResources) {

    @GetMapping("/api/beds/{bedId}/history")
    suspend fun handle(@PathVariable bedId: UUID): ResponseEntity<BedResourceWithHistory> =
        ResponseEntity.ok(resources.history(bedId))
}
