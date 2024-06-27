package org.starbornag.api.rest.bed

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.BedRepository
import org.starbornag.api.domain.bed.command.Dimensions
import java.net.URI
import java.util.*

@RestController
class PrepareBedCommandHandler {

    @PostMapping("/api/beds")
    suspend fun handle(@RequestBody command: PrepareBedCommand): ResponseEntity<BedResourceWithCurrentState> {
        val bedResource = createResource(command)
        return ResponseEntity.created(URI("/api/beds/${bedResource.id}")).body(bedResource)
    }

    private fun createResource(command: PrepareBedCommand) : BedResourceWithCurrentState {
        val bed = BedAggregate.of(command.bedId, command.name, command.dimensions, command.cellBlockSize)
        BedRepository.addBed(bed)
        val resource = BedResourceWithCurrentState.from(bed)
        resource.includeSchemas()
        return resource
    }
}