//package org.starbornag.api.rest.bed
//
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//import org.starbornag.api.domain.bed.BedCommands.*
//import reactor.core.publisher.Mono
//import java.net.URI
//import java.util.*
//
//@RestController
//@RequestMapping("/api/beds/{bedId}/plant")
//class PlantSeedlingInBedCommandHandler {
//
//    @PostMapping
//    fun handle(@RequestBody command: PlantSeedlingInBedCommand): Mono<ResponseEntity<BedResource>> {
//        val bedId = command.bedId
//        val plantingId = UUID.randomUUID()
//        return Mono.just(ResponseEntity.created(URI("/api/beds/$bedId/plantings/$plantingId"))
//            .body(updateResource(command, bedId)))
//    }
//
//    private fun updateResource(command: PlantSeedlingInBedCommand, plantindId: UUID): BedResource? {
//        val bedId = command.bedId
//        val bed = BedsRepository.getBed(command.bedId)
//            bed?.execute(command)
//        return BedResource.from(bed!!)
//    }
//}