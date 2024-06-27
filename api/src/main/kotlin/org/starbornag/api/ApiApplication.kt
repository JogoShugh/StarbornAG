package org.starbornag.api

import ch.rasc.sse.eventbus.config.EnableSseEventBus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedRepository
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.Dimensions
import java.util.*

@SpringBootApplication(scanBasePackages = ["org.starbornag.api", "org.starbornag.api.rest.bed"])
@EnableSseEventBus
class ApiApplication

fun main(args: Array<String>) {
	runApplication<ApiApplication>(*args)
}
