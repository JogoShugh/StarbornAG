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
    fun handle(@PathVariable bedId: UUID): Mono<ResponseEntity<BedResourceWithCurrentState>> {
        val bed = BedRepository.getBed(bedId)
        val resource = BedResourceWithCurrentState.from(bed!!)
        return Mono.just(ResponseEntity.ok(resource))
    }
}