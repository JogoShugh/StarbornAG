package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.BedRepository
import reactor.core.publisher.Mono
import java.util.*

@RestController
class BedCurrentStateQueryHandler {

    @GetMapping("/api/beds/{bedId}")
    suspend fun handle(@PathVariable bedId: UUID): ResponseEntity<BedResourceWithCurrentState> {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithCurrentState.from(bed!!)
        return ResponseEntity.ok(resource)
    }
}