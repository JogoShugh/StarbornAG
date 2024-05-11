package org.starbornag.api.rest.bed

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedRepository
import reactor.core.publisher.Mono
import java.util.*

@RestController
class BedCommandHandler(
    private val bedCommandMapper: BedCommandMapper
) {
    @PostMapping("/api/beds/{bedId}/{action}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun handle(
        @PathVariable bedId: UUID,
        @PathVariable action: String,
        @RequestBody commandPayload: Any
    ): Mono<ResponseEntity<BedResourceWithCurrentState>> {
        try {
            val bed = BedRepository.getBed(bedId)
            val command = bedCommandMapper.convertCommand(action, commandPayload)
            bed?.execute(command) // Execute the command directly
            val resource = BedResourceWithCurrentState.from(bed!!)
            val response = ResponseEntity.ok(resource)
            return Mono.just(response)
        } catch(e: Exception) {
            println("Here is the exception: " + e.stackTraceToString())
            return Mono.error(e);
        }
    }
}