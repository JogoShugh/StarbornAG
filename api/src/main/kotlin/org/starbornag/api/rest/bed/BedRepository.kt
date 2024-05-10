package org.starbornag.api.rest.bed

import java.util.*

object BedRepository {
    private val beds = mutableMapOf<UUID, BedAggregate>()
    fun getBed(bedId: UUID) = beds[bedId]
    fun addBed(bed: BedAggregate) {
        beds[bed.id] = bed
    }
}