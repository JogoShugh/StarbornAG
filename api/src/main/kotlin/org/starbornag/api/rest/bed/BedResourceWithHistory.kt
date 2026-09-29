package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.command.Row
import java.util.*

class BedResourceWithHistory(
    id: UUID,
    val name: String,
    val rows: List<Row>
) : BedResource<BedResourceWithHistory>(id) {
    companion object {
        fun from(bed: Bed) = BedResourceWithHistory(bed.id, bed.name, bed.rows.map(::Row))
    }
}
