package org.starbornag.api

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedRepository
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.api.domain.bed.command.Dimensions
import java.util.*

@Component
class Startup : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        arrayOf(
            PrepareBed(
                UUID.fromString("c0e75294-4b1e-4664-9037-3ca56f41ac5a"),
                "Earth",
                Dimensions(5, 10, 1),
                1
            ),
            PrepareBed(
                UUID.fromString("2fbda883-d49d-4067-8e16-2b04cc523111"),
                "Jupiter",
                Dimensions(5, 10, 2),
                1
            )
        ).forEach {
            val bed = BedAggregate.of(it.bedId, it.name, it.dimensions, it.cellBlockSize)
            println("The bed: $bed")
            println("\t${bed.name} : ${bed.id}")
            BedRepository.addBed(bed)
        }
    }
}