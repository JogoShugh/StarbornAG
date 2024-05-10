package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedCommand
import java.util.*

class BedAggregate(
    val id: UUID,
    val name: String,
    val rows: List<Row>
) {
    companion object {
        fun of(name: String, cellsPerRowCount: Int, rowCount: Int): BedAggregate {
            val start = 1
            val rows =
                start.rangeTo(rowCount).map {
                    Row(start.rangeTo(cellsPerRowCount).map { "" }.toMutableList())
                }

            return BedAggregate(UUID.randomUUID(), name, rows)
        }
    }

    val events = mutableListOf<BedEvent>()

    val waterings: List<BedWatered>
        get() = events.filterIsInstance<BedWatered>()

    val fertilizations: List<BedFertilized>
        get() = events.filterIsInstance<BedFertilized>()

    // Generic command handler dispatcher
    fun <T : BedCommand> execute(command: T) {
        when (command) {
            is BedCommand.PlantSeedlingInBedCommand -> execute(command)
            is BedCommand.WaterBedCommand -> execute(command)
            is BedCommand.FertilizeBedCommand -> execute(command)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: BedCommand.PlantSeedlingInBedCommand) {
        // Adjust to be 1 based:
        val rowPosition = command.rowPosition - 1
        val cellPositionInRow = command.cellPositionInRow - 1
        val cells = rows[rowPosition].cells
        cells[cellPositionInRow] = "${command.plantType} - ${command.plantCultivar}"
    }

    private fun execute(command: BedCommand.WaterBedCommand) {
        val wateredEvent = BedWatered(command.started, command.volume)
        events.add(wateredEvent)
    }

    private fun execute(command: BedCommand.FertilizeBedCommand) {
        val fertilizedEvent = BedFertilized(command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
    }
}