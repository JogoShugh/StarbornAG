package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedAggregate
import java.util.*

class BedResourceWithCurrentState(id: UUID,
                                  val name: String,
                                  val rows: List<Row>,
                                  waterings: List<BedWatered>?,
                                  fertilizations: List<BedFertilized>?
    ) : BedResource<BedResourceWithCurrentState>(id) {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithCurrentState(
            bed.id,
            bed.name,
            bed.rows,
            bed.waterings,
            bed.fertilizations
        )
    }

    val lastWatering: BedWatered?

    val lastFertilization: BedFertilized?

    init {
        lastWatering = waterings?.lastOrNull()
        lastFertilization = fertilizations?.lastOrNull()
    }
}

