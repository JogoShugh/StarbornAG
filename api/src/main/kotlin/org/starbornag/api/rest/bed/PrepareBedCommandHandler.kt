package org.starbornag.api.rest.bed

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.BedRepository
import java.net.URI

@RestController
class PrepareBedCommandHandler {

    @PostMapping("/api/beds")
    suspend fun handle(@RequestBody command: PrepareBed): ResponseEntity<BedResourceWithCurrentState> {
        val bedResource = createResource(command)
        return ResponseEntity.created(URI("/api/beds/${bedResource.id}")).body(bedResource)
    }

    private fun createResource(command: PrepareBed) : BedResourceWithCurrentState {
        val bed = BedAggregate.of(command.bedId, command.name, command.dimensions, command.cellBlockSize)
        BedRepository.addBed(bed)
        val resource = BedResourceWithCurrentState.from(bed)
        resource.includeSchemas()
        return resource
    }
}