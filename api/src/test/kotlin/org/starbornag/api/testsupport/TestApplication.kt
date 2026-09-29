package org.starbornag.api.testsupport

import org.springframework.boot.builder.SpringApplicationBuilder
import org.starbornag.api.ApiApplication

/** The whole application on a random port, against the test PostgreSQL, started once per test run. */
object TestApplication {
    val baseUrl: String by lazy {
        val properties = TestPostgres.r2dbcProperties() + mapOf(
            "server.port" to "0",
            // Placeholder: no scenario here calls OpenAI.
            "spring.ai.openai.api-key" to "test-key-not-used"
        )
        // Command-line arguments outrank application.yml; default properties would not.
        val arguments = properties.map { (key, value) -> "--$key=$value" }.toTypedArray()
        val context = SpringApplicationBuilder(ApiApplication::class.java).run(*arguments)
        "http://localhost:${context.environment.getProperty("local.server.port")}"
    }
}
