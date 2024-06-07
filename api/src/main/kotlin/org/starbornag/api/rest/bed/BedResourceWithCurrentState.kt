package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.*
import java.util.*

data class BedResourceCell(
    val planting: Planting,
    val lastWatering: BedWatered?,
    val lastFertilization: BedFertilized?,
    val lastHarvest: BedHarvested?
)

data class BedResourceRow(
    val cells: List<BedResourceCell>
)

class BedResourceWithCurrentState(id: UUID,
                                  val name: String,
                                  val rows: List<BedResourceRow>
//                                  ,
//                                  waterings: List<BedWatered>?,
//                                  fertilizations: List<BedFertilized>?,
//                                  harvestings: List<BedHarvested>?
    ) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithCurrentState(
            bed.id,
            bed.name,
            bed.rows.map {
                BedResourceRow(it.cells.map { id ->
                    val cell = BedCellRepository.getBedCell(id)
                    BedResourceCell(
                        cell.planting,
                        cell.waterings.lastOrNull(),
                        cell.fertilizations.lastOrNull(),
                        cell.harvests.lastOrNull()
                    )
                })
            }
        )
    }

//    val lastWatering: BedWatered?
//
//    val lastFertilization: BedFertilized?
//
//    val lastHarvest: BedHarvested?
//
//    init {
//        lastWatering = waterings?.lastOrNull()
//        lastFertilization = fertilizations?.lastOrNull()
//        lastHarvest = harvestings?.lastOrNull()
//    }
}

