package org.starbornag.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.starbornag.api", "org.starbornag.api.rest.bed"])
class ApiApplication

fun main(args: Array<String>) {
	runApplication<ApiApplication>(*args)
}
