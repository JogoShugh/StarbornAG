package org.starbornag.api.domain.bed

import org.springframework.stereotype.Component
import java.util.*

@Component
object BedRepository {
    private val beds = mutableMapOf<UUID, BedAggregate>()
    fun getBed(bedId: UUID) = beds[bedId]
    fun addBed(bed: BedAggregate) {
        beds[bed.id] = bed
    }
}