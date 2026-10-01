package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonProperty
import org.starbornag.api.application.bed.LoadedBedCell
import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.CellStory
import org.starbornag.api.domain.bed.Journal
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
 * (a missing link is an edge of the bed), a link to the journal, forms that act on exactly the
 * cells in focus, and the most recent commands embedded. The links are the page's own addresses:
 * an agent and a browser ask the same address and get their own view of it.
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
    @get:JsonProperty("_forms") val forms: Map<String, HalForm>,
    @get:JsonProperty("_embedded") val embedded: Map<String, List<JournalEntry>> = emptyMap()
) {
    /**
     * Checks a submitted body against this focus's own form for [action]: every required field the
     * server does not fill itself must be there. Without a form (the action is not possible here) the
     * soil rules answer instead.
     *
     * @throws FormIncomplete naming the missing fields.
     */
    fun requireFieldsOf(action: String, fields: Map<String, Any?>) {
        val form = forms.values.firstOrNull { it.links.getValue("target").getValue("href").endsWith("/$action") }
            ?: return
        val missing = form.schema.get("required").map { it.asText() }
            .filter { it !in FILLED_BY_THE_SERVER && fields[it] == null }
        if (missing.isNotEmpty()) throw FormIncomplete(missing.sorted())
    }

    companion object {
        /** Fields the server sets from the address or the clock. */
        private val FILLED_BY_THE_SERVER = setOf("bedId", "location", "started")

        /** How many recent commands come embedded unless the client asks for another number. */
        const val RECENT = 10

        fun of(
            bed: Bed,
            focus: Focus,
            positions: List<CellPosition>,
            cells: List<LoadedBedCell>,
            recent: Int = RECENT
        ): FocusResource {
            val rows = bed.rows.size
            val columns = bed.rows.first().size
            val base = "/beds/${bed.id}"
            val links = mapOf(
                "self" to mapOf("href" to "$base/focus/${focus.path}", "title" to focus.label),
                "bed" to mapOf("href" to base, "title" to bed.name),
                "journal" to mapOf("href" to "$base/journal?focus=${focus.path}", "title" to "${focus.label} journal")
            ) + focus.moves(rows, columns).map { (move, next) ->
                // The title names where the move leads, for example "Row B" for zoom-out from B2.
                move.word to mapOf("href" to "$base/focus/${next.path}", "title" to next.label)
            }
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
                links = links,
                forms = HalSchemaForms.forCells(
                    bed, cells.map { it.state }, focus.spokenLocation(rows, columns), "$base/focus/${focus.path}"
                ),
                embedded = mapOf("recent" to recentCommands(positions, cells, recent))
            )
        }

        private fun recentCommands(positions: List<CellPosition>, cells: List<LoadedBedCell>, recent: Int) =
            Journal(positions.zip(cells) { position, loaded ->
                CellStory(position, loaded.state.plantings.lastOrNull() ?: Planting("", ""), loaded.history)
            }).commands().take(recent.coerceAtLeast(0)).map(JournalEntry::of)
    }
}
