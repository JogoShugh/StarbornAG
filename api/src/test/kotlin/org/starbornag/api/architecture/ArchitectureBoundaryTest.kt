package org.starbornag.api.architecture

import assertk.assertThat
import assertk.assertions.containsAtLeast
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/** bdd-gates: the hexagonal boundary as failing tests, not a paragraph. */
class ArchitectureBoundaryTest {

    private val productionFiles
        get() = Konsist.scopeFromProject().files.filter { it.path.contains("/api/src/main/kotlin/") }

    private val domainFiles
        get() = productionFiles.filter { it.hasPackage("org.starbornag.api.domain..") }

    private val applicationFiles
        get() = productionFiles.filter { it.hasPackage("org.starbornag.api.application..") }

    private val infrastructure = listOf("org.springframework", "ch.rasc", "io.r2dbc", "org.postgresql")

    // Guards the other rules: an empty scope would make every assertFalse pass vacuously.
    @Test
    fun `the scope contains the domain and adapter files`() {
        assertThat(domainFiles.map { it.name }).containsAtLeast("Bed", "BedCell", "BedEvent")
        assertThat(productionFiles.map { it.name }).containsAtLeast("EventStoreConfig", "BedCommandHandler")
        assertThat(applicationFiles.map { it.name }).containsAtLeast("Beds", "BedCells", "BedEventPublisher")
    }

    @Test
    fun `the domain imports no framework, infrastructure, adapter or application types`() {
        val forbidden = infrastructure + listOf(
            "org.starbornag.eventstore",
            "org.starbornag.api.rest",
            "org.starbornag.api.sse",
            "org.starbornag.api.application"
        )
        domainFiles
            .assertFalse { file -> file.hasImport { import -> forbidden.any { import.name.startsWith(it) } } }
    }

    @Test
    fun `the application layer uses ports, not frameworks or adapters`() {
        val forbidden = infrastructure + listOf("org.starbornag.api.rest", "org.starbornag.api.sse")
        applicationFiles.assertFalse { file ->
            file.hasImport { import -> forbidden.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `only the event store configuration chooses the PostgreSQL adapter`() {
        productionFiles
            .filter { it.name != "EventStoreConfig" }
            .assertFalse { file -> file.hasImport { it.name == "org.starbornag.eventstore.PgEventStore" } }
    }
}
