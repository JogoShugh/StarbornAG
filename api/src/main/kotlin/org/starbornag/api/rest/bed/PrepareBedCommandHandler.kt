package org.starbornag.api.rest.bed

import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedCommand.*
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/beds")
class PrepareBedCommandHandler {

    @PostMapping
    fun handle(@RequestBody command: PrepareBedCommand): Mono<ResponseEntity<BedResource>> {
        val bedResource = createResource(command)
        return Mono.just(ResponseEntity.created(URI("/api/beds/${bedResource.id}"))
            .body(bedResource))
    }

    private fun createResource(command: PrepareBedCommand) : BedResource {
        val bed = BedAggregate.of(command.name, command.dimensions.length, command.dimensions.width)
//                    .withRel("plant")
//                    .withAffordances(Affordances)

        BedsRepository.addBed(bed)

        return BedResource.from(bed).add(Links.of(
            Link.of("/api/beds/${bed.id}").withSelfRel(),
            Link.of("/api/beds/${bed.id}/plant")
        ))
    }
}