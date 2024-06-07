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
class BedHistoryQueryHandler {

    @GetMapping("/api/beds/{bedId}/history")
    fun handle(@PathVariable bedId: UUID): Mono<ResponseEntity<BedResourceWithHistory>> {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithHistory.from(bed!!)
        return Mono.just(ResponseEntity.ok(resource))
    }
}