package org.starbornag.api.domain.bed

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.starbornag.api.testsupport.TestPostgres
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.CellsSelection
import java.time.Instant
import java.util.*

class BedCellAggregateEventSourcedTest {

    private class DummyBus : IBedEventBus {
        override suspend fun publishEvent(command: BedCommand.CellCommand, event: BedEvent) {
        }

        override suspend fun storeEvent(event: BedEvent) {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `it can save new events and fetch them back to populate an aggregate`() = runBlocking<Unit> {
        val dummyBus = DummyBus()
        val repository = BedCellStateRepository(TestPostgres.freshEventStore())
        val bedId = UUID.randomUUID()
        val cellId = UUID.randomUUID()

        val cellAggregate = BedCellAggregateEventSourced.of(repository, bedId, cellId)

        val plant = BedCommand.CellCommand.PlantSeedling(bedId,
            started = Date.from(Instant.now()),
            plantType = "Tomato",
            plantCultivar = "Dark Galaxy",
            location = CellsSelection.fromString("A1")
        )

        // This cheats by bypassing the Bed facade aggregate:
        cellAggregate.execute(plant, dummyBus)

        val waterAgesAgo = BedCommand.CellCommand.Water(bedId,
            started = 3.months.ago,
            volume = 1.0,
            location = CellsSelection.fromString("A1")
        )
        cellAggregate.execute(waterAgesAgo, dummyBus)

        val justNow = now
        val water = BedCommand.CellCommand.Water(bedId,
            started = justNow,
            volume = 1.0,
            location = CellsSelection.fromString("A1")
        )
        cellAggregate.execute(water, dummyBus)

        val aggAfterSave = BedCellAggregateEventSourced.of(repository, bedId, cellId)

        assertThat(aggAfterSave.state.plantings).containsExactly(Planting("Tomato", "Dark Galaxy"))
        // The watering from 3 months ago is too old to count as current state; the recent one wins.
        assertThat(aggAfterSave.state.watered!!.started).isEqualTo(justNow)
    }
}