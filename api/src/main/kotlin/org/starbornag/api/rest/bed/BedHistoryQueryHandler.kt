package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedRepository
import java.util.*

@RestController
class BedHistoryQueryHandler {

    @GetMapping("/api/beds/{bedId}/history")
    suspend fun handle(@PathVariable bedId: UUID): ResponseEntity<BedResourceWithHistory> {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithHistory.from(bed!!)
        return ResponseEntity.ok(resource)
    }
}