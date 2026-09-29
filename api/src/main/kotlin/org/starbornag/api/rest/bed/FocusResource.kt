package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonProperty
import org.starbornag.api.application.bed.LoadedBedCell
import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Planting
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.rowLetter
import java.util.*

data class FocusCell(
    val position: String,
    val bedCellId: UUID,
    val planting: Planting,
    val lastWatering: BedCellWatered?
)

/**
 * Where the gardener stands in a bed, as HAL with HAL Schema Forms: a link for every possible move
 * (a missing link is an edge of the bed), and forms that act on exactly the cells in focus.
 */
data class FocusResource(
    val bedId: UUID,
    val bedName: String,
    val focus: String,
    val path: String,
    /** The focus as a spoken location, such as "B1 to B8"; null for the whole bed. */
    val location: String?,
    val cells: List<FocusCell>,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>,
    @get:JsonProperty("_forms") val forms: Map<String, HalForm>
) {
    companion object {
        fun of(bed: Bed, focus: Focus, positions: List<CellPosition>, cells: List<LoadedBedCell>): FocusResource {
            val rows = bed.rows.size
            val columns = bed.rows.first().size
            val base = "/api/beds/${bed.id}"
            val links = mapOf("self" to "$base/focus/${focus.path}", "bed" to base) +
                focus.moves(rows, columns).map { (move, next) -> move.word to "$base/focus/${next.path}" }
            return FocusResource(
                bedId = bed.id,
                bedName = bed.name,
                focus = focus.label,
                path = focus.path,
                location = focus.spokenLocation(rows, columns),
                cells = positions.zip(cells) { position, loaded ->
                    FocusCell(
                        "${rowLetter(position.row)}${position.column}",
                        loaded.state.id,
                        loaded.state.plantings.lastOrNull() ?: Planting("", ""),
                        loaded.state.lastWatered
                    )
                },
                links = links.mapValues { (_, href) -> mapOf("href" to href) },
                forms = HalSchemaForms.forCells(bed, cells.map { it.state }, focus.spokenLocation(rows, columns))
            )
        }
    }
}
