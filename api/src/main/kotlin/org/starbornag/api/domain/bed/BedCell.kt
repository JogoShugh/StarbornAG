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

    /** The type of plant growing now, in lower case, or null for an empty cell. */
    val currentPlantType: String? get() = plantings.lastOrNull()?.plantType?.lowercase()

    fun isGrowing(plantType: String): Boolean = currentPlantType == plantType.lowercase()

    companion object {
        /**
         * The soil rules for a command over the [named] cells: planting needs every named cell to be
         * empty, and a harvest applies only to the named cells growing the harvested plant.
         * Returns the cells to record on, or rejects the whole command.
         */
        fun targets(command: CellCommand, named: List<BedCell>): List<BedCell> =
            when (command) {
                is CellCommand.PlantSeedling -> named.also { cells ->
                    cells.filter { it.isPlanted }.map { it.id }.ifEmpty { null }?.let { throw CellAlreadyPlanted(it) }
                }
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

/**
 * Planting is refused because these cells already grow something. Named by [labels] ("A1") once the
 * bed has placed them (see [locatedIn]), otherwise by id.
 */
class CellAlreadyPlanted(val cellIds: List<UUID>, val labels: List<String> = emptyList()) :
    IllegalStateException(sayPlanted(labels.ifEmpty { cellIds.map { "Cell $it" } })) {

    /** The same refusal, with each cell named by its place in [bed]. */
    fun locatedIn(bed: Bed) = CellAlreadyPlanted(cellIds, cellIds.map { id ->
        bed.positionOf(id)?.let { "${rowLetter(it.row)}${it.column}" } ?: "Cell $id"
    })
}

/** "A1 is already planted", "A1, A2 and A3 are already planted". */
private fun sayPlanted(names: List<String>): String = when (names.size) {
    1 -> "${names[0]} is already planted"
    else -> "${names.dropLast(1).joinToString(", ")} and ${names.last()} are already planted"
}

class NothingToHarvest(val plantType: String) :
    IllegalStateException("None of the named cells is growing $plantType")
