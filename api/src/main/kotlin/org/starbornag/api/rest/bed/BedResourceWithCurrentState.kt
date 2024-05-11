package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedWatered
import org.starbornag.api.domain.bed.Row
import java.util.*

class BedResourceWithCurrentState(id: UUID,
                                  val name: String,
                                  val rows: List<Row>,
                                  waterings: List<BedWatered>?,
                                  fertilizations: List<BedFertilized>?,
                                  harvestings: List<BedHarvested>?
    ) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithCurrentState(
            bed.id,
            bed.name,
            bed.rows,
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

