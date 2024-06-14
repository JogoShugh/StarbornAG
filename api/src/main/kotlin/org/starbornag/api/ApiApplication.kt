package org.starbornag.api

import ch.rasc.sse.eventbus.config.EnableSseEventBus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.starbornag.api", "org.starbornag.api.rest.bed"])
@EnableSseEventBus
class ApiApplication

fun main(args: Array<String>) {
	runApplication<ApiApplication>(*args)
}
