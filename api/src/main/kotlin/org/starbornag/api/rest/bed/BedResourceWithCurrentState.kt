package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.*
import org.starbornag.api.domain.bed.command.Row
import java.util.*

data class BedResourceRow(
    val cells: List<String>
)

class BedResourceWithCurrentState(id: UUID,
                                  val name: String,
                                  val rows: List<BedResourceRow>,
                                  waterings: List<BedWatered>?,
                                  fertilizations: List<BedFertilized>?,
                                  harvestings: List<BedHarvested>?
    ) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithCurrentState(
            bed.id,
            bed.name,
            bed.rows.map {
                BedResourceRow(it.cells.map { id ->
                    val cell = BedCellRepository.getBedCell(id)
                    if (cell.plantType == "") "" else "${cell.plantType} - ${cell.plantCultivar}"
                })
            },
            bed.waterings,
            bed.fertilizations,
            bed.harvests
        )
    }

    val lastWatering: BedWatered?

    val lastFertilization: BedFertilized?

    val lastHarvest: BedHarvested?

    init {
        lastWatering = waterings?.lastOrNull()
        lastFertilization = fertilizations?.lastOrNull()
        lastHarvest = harvestings?.lastOrNull()
    }
}

