package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedAggregate
import java.util.*

class BedResourceWithHistory(id: UUID,
                             val name: String,
                             val rows: List<Row>,
                             val waterings: List<BedWatered>,
                             val fertilizations: List<BedFertilized>
) : BedResource<BedResourceWithHistory>(id)  {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithHistory(
            bed.id,
            bed.name,
            bed.rows,
            bed.waterings,
            bed.fertilizations
        )
    }
}