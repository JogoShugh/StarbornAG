package org.starbornag.api.rest.bed

import org.springframework.stereotype.Component
import org.starbornag.api.application.bed.BedCells
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.application.bed.UnknownBed
import org.starbornag.api.domain.bed.Bed
import java.util.*

/**
 * Builds the bed representations from the use cases: the bed's layout plus each cell's state and
 * history, read from their streams. Slice 5 replaces the per-cell reads with a projection.
 */
@Component
class BedResources(private val beds: Beds, private val bedCells: BedCells) {

    /** @throws UnknownBed when no bed has this id. */
    suspend fun bed(bedId: UUID): Bed = beds.find(bedId) ?: throw UnknownBed(bedId)

    suspend fun currentState(bedId: UUID): BedResourceWithCurrentState = currentState(bed(bedId))

    suspend fun currentState(bed: Bed): BedResourceWithCurrentState =
        BedResourceWithCurrentState.from(bed, bed.rows.map { row -> row.map { bedCells.load(bed.id, it) } })

    suspend fun history(bedId: UUID): BedResourceWithHistory = BedResourceWithHistory.from(bed(bedId))
}
