package org.starbornag.api

import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.api.domain.bed.command.Dimensions
import java.util.*

/** Prepares the demo beds Earth and Jupiter, with fixed ids, the first time the application starts. */
@Component
class Startup(private val beds: Beds) : ApplicationListener<ApplicationReadyEvent> {

    private val demoBeds = listOf(
        PrepareBed(UUID.fromString("c0e75294-4b1e-4664-9037-3ca56f41ac5a"), "Earth", Dimensions(5, 10, 1), 1),
        PrepareBed(UUID.fromString("2fbda883-d49d-4067-8e16-2b04cc523111"), "Jupiter", Dimensions(5, 10, 2), 1)
    )

    override fun onApplicationEvent(event: ApplicationReadyEvent) = runBlocking {
        demoBeds
            .filter { beds.find(it.bedId) == null }
            .forEach { beds.prepare(it) }
    }
}
