package org.starbornag.api.rest.bed

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import java.net.URI

@RestController
class PrepareBedCommandHandler(private val beds: Beds, private val resources: BedResources) {

    /** Records the bed and its cell layout; answers 409 when the bed already exists. */
    @PostMapping("/api/beds")
    suspend fun handle(@RequestBody command: PrepareBed): ResponseEntity<BedResourceWithCurrentState> {
        val bed = beds.prepare(command)
        return ResponseEntity.created(URI("/api/beds/${bed.id}"))
            .header(HttpHeaders.LINK, BedResources.PROFILE_LINK)
            .body(resources.currentState(bed))
    }
}
