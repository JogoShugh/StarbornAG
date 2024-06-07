package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.Dimensions
import org.starbornag.api.domain.bed.command.Row
import java.util.*

class BedAggregate(
    val id: UUID,
    val name: String,
    val rows: List<Row>
) {
    companion object {
        fun of(id: UUID, name: String, dimensions: Dimensions, cellBlockSize: Int): BedAggregate {
            val start = 1
            val rows =
                start.rangeTo(dimensions.width).map {
                    Row(start.rangeTo(dimensions.length / cellBlockSize).map {
                        val bc = BedCellAggregate(id)
                        BedCellRepository.putBedCell(bc)
                        bc.id
                    })
                }

            return BedAggregate(id, name, rows)
        }
    }

    val events = mutableListOf<BedEvent>()

    val waterings: List<BedWatered>
        get() = events.filterIsInstance<BedWatered>()

    val fertilizations: List<BedFertilized>
        get() = events.filterIsInstance<BedFertilized>()

    val harvests: List<BedHarvested>
        get() = events.filterIsInstance<BedHarvested>()

    // Generic command handler dispatcher
    fun <T : BedCommand> execute(command: T) {
        when (command) {
            is PlantSeedlingInBedCommand -> execute(command)
            is WaterBedCommand -> execute(command)
            is FertilizeBedCommand -> execute(command)
            is HarvestBedCommand -> execute(command)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: PlantSeedlingInBedCommand) {
        // Adjust to be 1 based:
        val rowPosition = command.rowPosition - 1
        val cellPositionInRow = command.cellPositionInRow - 1
        val row = rows[rowPosition]
        val cellId = row.cells[cellPositionInRow]
        val bedCell = BedCellRepository.getBedCell(cellId)
        val updatedBedCell = bedCell!!.copy(plantType = command.plantType, plantCultivar = command.plantCultivar)
        BedCellRepository.putBedCell(updatedBedCell)
    }

    private fun execute(command: WaterBedCommand) {
        val wateredEvent = BedWatered(command.started, command.volume)
        events.add(wateredEvent)
    }

    private fun execute(command: FertilizeBedCommand) {
        val fertilizedEvent = BedFertilized(command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
    }

    private fun execute(command: HarvestBedCommand) {
        val harvestedFromBedEvent = BedHarvested(
            command.started,
            command.plantType,
            command.plantCultivar,
            command.quantity,
            command.weight
        )
        events.add(harvestedFromBedEvent)
    }
}