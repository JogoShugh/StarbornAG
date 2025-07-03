package org.starbornag.api.domain.bed

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
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
    fun `it can saved new events and fetch them back to populate an aggregate`() = runBlocking {
        val dummyBus = DummyBus()
        val bedId = UUID.randomUUID()
        val cellId = UUID.randomUUID()

        val cellAggregate = BedCellAggregateEventSourced.of(bedId, cellId)

        val plant = BedCommand.CellCommand.PlantSeedling(bedId,
            Date.from(Instant.now()),
            "Tomato",
            "Dark Galaxy",
            CellsSelection.fromString("A1")
        )

        // This cheats by bypassing the Bed facade aggregate:
        cellAggregate.execute(plant, dummyBus)

        val waterAgesAgo = BedCommand.CellCommand.Water(bedId,
            3.months.ago,
            1.0,
            CellsSelection.fromString("A1")
        )
        cellAggregate.execute(waterAgesAgo, dummyBus)

        val water = BedCommand.CellCommand.Water(bedId,
            now, // a bit weird, check the definition of now...
            1.0,
            CellsSelection.fromString("A1")
        )
        cellAggregate.execute(water, dummyBus)

        val aggAfterSave = BedCellAggregateEventSourced.of(bedId, cellId)

        println(aggAfterSave)
        //assertThat(aggAfterSave).isNotNull
    }
}