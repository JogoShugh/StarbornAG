package org.starbornag.api.rest.bed

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.starbornag.api.application.bed.BedCells
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.application.bed.UnknownBed
import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.Focus
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

    /** The bed's cells with their history, plus a form for every care action possible right now. */
    suspend fun currentState(bed: Bed): BedResourceWithCurrentState {
        val cells = bed.rows.map { row -> row.map { bedCells.load(bed.id, it) } }
        return BedResourceWithCurrentState.from(bed, cells).apply {
            forms.putAll(HalSchemaForms.forCells(bed, cells.flatten().map { it.state }))
        }
    }

    /** Wraps a bed representation with the HAL Schema Forms profile link. */
    fun <T> ok(body: T): ResponseEntity<T> = ResponseEntity.ok().header(HttpHeaders.LINK, PROFILE_LINK).body(body)

    companion object {
        const val PROFILE_LINK = "<${HalSchemaForms.PROFILE}>; rel=\"profile\""
    }

    suspend fun history(bedId: UUID): BedResourceWithHistory = BedResourceWithHistory.from(bed(bedId))

    /** The journal of the focus at [path], folded [by] cell, row, column or time. */
    suspend fun journal(bedId: UUID, path: String, by: String): JournalResource {
        val state = currentState(bedId)
        val focus = Focus.fromPath(path, state.rows.size, state.rows.first().cells.size)
        return JournalResource.of(bedId, focus, by, state.journal(focus))
    }

    /**
     * The focus at [path] ("cell/B4", "row/B", "column/4" or "bed"), with only its cells loaded.
     *
     * @throws org.starbornag.api.domain.bed.FocusOutsideBed when the focus is not part of the bed.
     */
    suspend fun focus(bedId: UUID, path: String, recent: Int = FocusResource.RECENT): FocusResource {
        val bed = bed(bedId)
        val rows = bed.rows.size
        val columns = bed.rows.first().size
        val focus = Focus.fromPath(path, rows, columns)
        val positions = focus.cells(rows, columns)
        val cells = positions.map { bedCells.load(bed.id, bed.rows[it.row - 1][it.column - 1]) }
        return FocusResource.of(bed, focus, positions, cells, recent)
    }
}
