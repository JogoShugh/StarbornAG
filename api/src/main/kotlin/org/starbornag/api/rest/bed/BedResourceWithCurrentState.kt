package org.starbornag.api.rest.bed

import org.starbornag.api.application.bed.LoadedBedCell
import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.Planting
import java.util.*

data class BedResourceCell(
    val bedCellId: UUID,
    val planting: Planting,
    val events: List<BedEvent>,
    val lastWatering: BedCellWatered?,
    val lastFertilization: BedFertilized?,
    val lastHarvest: BedHarvested?
)

data class BedResourceRow(
    val cells: List<BedResourceCell>
)

class BedResourceWithCurrentState(
    id: UUID,
    val name: String,
    val rows: List<BedResourceRow>
) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        /** [cells] holds each cell's loaded state and history, row by row, in the bed's layout. */
        fun from(bed: Bed, cells: List<List<LoadedBedCell>>) = BedResourceWithCurrentState(
            bed.id,
            bed.name,
            cells.map { row ->
                BedResourceRow(row.map { loaded ->
                    val cell = loaded.state
                    BedResourceCell(
                        cell.id,
                        cell.plantings.lastOrNull() ?: Planting("", ""),
                        loaded.history,
                        cell.lastWatered,
                        cell.lastFertilized,
                        cell.harvests.lastOrNull()
                    )
                })
            }
        )
    }
}
