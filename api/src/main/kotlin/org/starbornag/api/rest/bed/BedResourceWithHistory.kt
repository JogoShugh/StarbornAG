package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedAggregate
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedWatered
import org.starbornag.api.domain.bed.Row
import java.util.*

class BedResourceWithHistory(id: UUID,
                             val name: String,
                             val rows: List<Row>,
                             val waterings: List<BedWatered>,
                             val fertilizations: List<BedFertilized>,
                             val harvests: List<BedHarvested>
) : BedResource<BedResourceWithHistory>(id)  {
    companion object {
        fun from(bed: BedAggregate) = BedResourceWithHistory(
            bed.id,
            bed.name,
            bed.rows,
            bed.waterings,
            bed.fertilizations,
            bed.harvests
        )
    }
}