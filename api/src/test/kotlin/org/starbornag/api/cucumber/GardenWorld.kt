package org.starbornag.api.cucumber

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.application.bed.Cells
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.testsupport.TestPostgres
import java.util.*

/**
 * Per-scenario state shared by step classes through picocontainer: the application's use cases
 * over a real event store in a fresh schema, and the ids behind the names used in Gherkin.
 */
class GardenWorld {
    private val connectionFactory: ConnectionFactory = blocking { TestPostgres.freshSchema() }

    lateinit var beds: Beds
        private set
    lateinit var cells: Cells
        private set

    private val bedIds = mutableMapOf<String, UUID>()

    /** Everything the application announced, through a recording adapter of the publisher port. */
    val announcements: MutableList<BedEvent> = Collections.synchronizedList(mutableListOf())

    init {
        restart()
    }

    fun bedId(name: String): UUID = bedIds.getOrPut(name) { UUID.randomUUID() }

    /** A new set of use cases over the same database: nothing survives but what was stored. */
    fun restart() = blocking {
        val eventStore = TestPostgres.eventStore(connectionFactory)
        beds = Beds(eventStore)
        cells = Cells(eventStore, beds) { events -> announcements += events }
    }

    fun <T> blocking(block: suspend CoroutineScope.() -> T): T = runBlocking(block = block)
}
