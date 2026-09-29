package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import java.util.*

/**
 * Aggregate root for one square foot of a bed. It keeps the long-lived history of that soil in
 * its own stream (stream id = cell id); state is rebuilt with [evolve] and each command is turned
 * into the cell's events with [decide].
 */
data class BedCell(
    val id: UUID,
    val bedId: UUID,
    val plantings: List<Planting> = emptyList(),
    val lastWatered: BedCellWatered? = null,
    val lastFertilized: BedFertilized? = null,
    val lastMulched: BedMulched? = null,
    val harvests: List<BedHarvested> = emptyList()
) {
    /** A cell holds one planting at a time; harvesting does not empty it. */
    val isPlanted: Boolean get() = plantings.isNotEmpty()

    fun isGrowing(plantType: String): Boolean =
        plantings.lastOrNull()?.plantType.equals(plantType, ignoreCase = true)

    companion object {
        /**
         * The soil rules for a command over the [named] cells: planting needs every named cell to be
         * empty, and a harvest applies only to the named cells growing the harvested plant.
         * Returns the cells to record on, or rejects the whole command.
         */
        fun targets(command: CellCommand, named: List<BedCell>): List<BedCell> =
            when (command) {
                is CellCommand.PlantSeedling -> named.onEach { if (it.isPlanted) throw CellAlreadyPlanted(it.id) }
                is CellCommand.Harvest -> named
                    .filter { it.isGrowing(command.plantType) }
                    .ifEmpty { throw NothingToHarvest(command.plantType) }
                else -> named
            }

        fun evolve(cell: BedCell?, event: BedEvent): BedCell {
            val current = cell ?: BedCell(event.bedCellId, event.bedId)
            return when (event) {
                is BedCellPlanted ->
                    current.copy(plantings = current.plantings + Planting(event.plantType, event.plantCultivar))
                is BedCellWatered -> current.copy(lastWatered = event)
                is BedFertilized -> current.copy(lastFertilized = event)
                is BedMulched -> current.copy(lastMulched = event)
                is BedHarvested -> current.copy(harvests = current.harvests + event)
            }
        }

        /** Records one event on the cell, after checking the soil rules against its current state. */
        fun decide(bedId: UUID, cellId: UUID, command: CellCommand): (BedCell?) -> List<BedEvent> = { cell ->
            targets(command, listOf(cell ?: BedCell(cellId, bedId)))
            listOf(eventFor(bedId, cellId, command))
        }

        private fun eventFor(bedId: UUID, cellId: UUID, command: CellCommand): BedEvent =
            when (command) {
                is CellCommand.PlantSeedling ->
                    BedCellPlanted(bedId, cellId, command.started, command.plantType, command.plantCultivar)
                is CellCommand.Water -> BedCellWatered(bedId, cellId, command.started, command.volume)
                is CellCommand.Fertilize ->
                    BedFertilized(bedId, cellId, command.started, command.volume, command.fertilizer)
                is CellCommand.Mulch -> BedMulched(bedId, cellId, command.started, command.volume, command.material)
                is CellCommand.Harvest -> BedHarvested(
                    bedId, cellId, command.started, command.plantType, command.plantCultivar,
                    command.quantity, command.weight
                )
            }
    }
}

class CellAlreadyPlanted(val cellId: UUID) : IllegalStateException("Cell $cellId is already planted")

class NothingToHarvest(val plantType: String) :
    IllegalStateException("None of the named cells is growing $plantType")
