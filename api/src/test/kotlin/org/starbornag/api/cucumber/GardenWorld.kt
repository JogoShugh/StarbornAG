package org.starbornag.api.cucumber

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.testsupport.TestPostgres
import java.util.*

/**
 * Per-scenario state shared by step classes through picocontainer: the application's use cases
 * over a real event store in a fresh schema, and the ids behind the names used in Gherkin.
 */
class GardenWorld {
    private val connectionFactory: ConnectionFactory = blocking { TestPostgres.freshSchema() }

    var beds: Beds = startApplication()
        private set

    private val bedIds = mutableMapOf<String, UUID>()

    fun bedId(name: String): UUID = bedIds.getOrPut(name) { UUID.randomUUID() }

    /** A new set of use cases over the same database: nothing survives but what was stored. */
    fun restart() {
        beds = startApplication()
    }

    private fun startApplication() = blocking { Beds(TestPostgres.eventStore(connectionFactory)) }

    fun <T> blocking(block: suspend CoroutineScope.() -> T): T = runBlocking(block = block)
}
