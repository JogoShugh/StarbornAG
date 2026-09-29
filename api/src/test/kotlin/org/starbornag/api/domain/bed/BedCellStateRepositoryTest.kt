package org.starbornag.api.domain.bed

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.starbornag.api.testsupport.TestPostgres
import java.util.*

class BedCellStateRepositoryTest {

    @Test
    fun `it populates BedCellAggregateState from the cell's stored events`() = runBlocking {
        val repository = BedCellStateRepository(TestPostgres.freshEventStore())
        val bedId = UUID.randomUUID()
        val cellId = UUID.randomUUID()
        val events = buildBedEvents(bedId, cellId) {
            planted(2.months.ago, "Tomato", "Dark Galaxy")
            watered(2.days.ago, 2.0)
        }.toList()

        repository.append(cellId, events)
        val state = repository.fetch(cellId)

        assertThat(state.plantings!!).containsExactly(Planting("Tomato", "Dark Galaxy"))
        assertThat(state.watered!!.volume).isEqualTo(2.0)
        assertThat(state.fertilized).isNull()
    }

    @Test
    fun `an unknown cell has empty state`() = runBlocking {
        val repository = BedCellStateRepository(TestPostgres.freshEventStore())

        assertThat(repository.fetch(UUID.randomUUID())).isEqualTo(BedCellAggregateState())
    }
}
