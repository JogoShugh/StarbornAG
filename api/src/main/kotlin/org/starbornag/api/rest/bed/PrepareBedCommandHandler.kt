package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedCommand.*
import org.starbornag.api.domain.bed.BedRepository
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

@RestController
class PrepareBedCommandHandler {

    @PostMapping("/api/beds")
    fun handle(@RequestBody command: PrepareBedCommand): Mono<ResponseEntity<BedResourceWithCurrentState>> {
        val bedResource = createResource(command)
        return Mono.just(ResponseEntity.created(URI("/api/beds/${bedResource.id}"))
            .body(bedResource))
    }

    private fun createResource(command: PrepareBedCommand) : BedResourceWithCurrentState {
        val bed = BedAggregate.of(command.name, command.dimensions.length, command.dimensions.width)
        BedRepository.addBed(bed)
        return BedResourceWithCurrentState.from(bed)
    }
}