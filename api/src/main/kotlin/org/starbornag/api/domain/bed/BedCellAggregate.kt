package org.starbornag.api.domain.bed

import java.util.*

data class BedCellAggregate(
    val parentBedId: UUID,
    val id: UUID = UUID.randomUUID(),
    val plantType: String = "",
    val plantCultivar: String = ""
)
