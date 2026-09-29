package org.starbornag.api.testsupport

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.PgEventStore
import org.starbornag.eventstore.withSession
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

/** One real PostgreSQL for the whole test run, started on first use. */
object TestPostgres {
    private val container: PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:17-alpine").apply { start() }

    /** An initialized event store in a fresh schema of its own. */
    suspend fun freshEventStore(): EventStore {
        val schema = "test_" + UUID.randomUUID().toString().replace("-", "")
        val connectionFactory = PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host(container.host)
                .port(container.firstMappedPort)
                .database(container.databaseName)
                .username(container.username)
                .password(container.password)
                .schema(schema)
                .build()
        )
        connectionFactory.withSession { it.execute("CREATE SCHEMA $schema") }
        return PgEventStore(connectionFactory).also { it.init() }
    }

    /** Points a Spring Boot test's R2DBC connection at the container. */
    fun registerR2dbc(registry: DynamicPropertyRegistry) {
        registry.add("spring.r2dbc.url") {
            "r2dbc:postgresql://${container.host}:${container.firstMappedPort}/${container.databaseName}"
        }
        registry.add("spring.r2dbc.username") { container.username }
        registry.add("spring.r2dbc.password") { container.password }
    }
}
