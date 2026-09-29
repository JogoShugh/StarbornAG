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

    /**
     * Domain files that still reach into infrastructure, recorded when the gates were installed.
     * Shrink this list; never add to it. BedEventBus publishes to the SSE bus directly.
     */
    private val knownDomainViolations = setOf("BedEventBus")

    // Guards the other rules: an empty scope would make every assertFalse pass vacuously.
    @Test
    fun `the scope contains the domain and adapter files`() {
        assertThat(domainFiles.map { it.name }).containsAtLeast("BedAggregate", "BedCellAggregate", "BedEvent")
        assertThat(productionFiles.map { it.name }).containsAtLeast("EventStoreConfig", "BedCommandHandler")
    }

    @Test
    fun `the domain imports no framework or infrastructure types`() {
        val forbidden = listOf("org.springframework", "ch.rasc", "io.r2dbc", "org.postgresql", "org.starbornag.api.rest")
        domainFiles
            .filter { it.name !in knownDomainViolations }
            .assertFalse { file -> file.hasImport { import -> forbidden.any { import.name.startsWith(it) } } }
    }

    @Test
    fun `only the event store configuration chooses the PostgreSQL adapter`() {
        productionFiles
            .filter { it.name != "EventStoreConfig" }
            .assertFalse { file -> file.hasImport { it.name == "org.starbornag.eventstore.PgEventStore" } }
    }
}
