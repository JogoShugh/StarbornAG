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
    companion object {
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

        /** Every care command records exactly one event on the cell. */
        fun decide(bedId: UUID, cellId: UUID, command: CellCommand): (BedCell?) -> List<BedEvent> = { _ ->
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
