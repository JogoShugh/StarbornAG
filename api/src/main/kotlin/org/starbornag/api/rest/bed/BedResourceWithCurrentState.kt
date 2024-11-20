package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.*
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

class BedResourceWithCurrentState(id: UUID,
                                  val name: String,
                                  val rows: List<BedResourceRow>,
//                                  waterings: List<BedCellWatered>?,
//                                  fertilizations: List<BedFertilized>?,
//                                  harvestings: List<BedHarvested>?
    ) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithCurrentState(
            bed.id, //28453a3c-ef59-40fa-a31c-9b30ae938b9e
            bed.name,
            bed.rows.map {
                BedResourceRow(it.cells.map { id ->
                    val cell = BedCellRepository.getBedCell(id)
                    BedResourceCell(
                        cell.id,
                        cell.planting,
                        cell.events,
                        cell.waterings.lastOrNull(),
                        cell.fertilizations.lastOrNull(),
                        cell.harvests.lastOrNull()
                    )
                })
            }
        )
    }

//    val lastWatering: BedCellWatered?
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

