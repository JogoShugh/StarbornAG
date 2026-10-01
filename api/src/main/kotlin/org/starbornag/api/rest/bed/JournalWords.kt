package org.starbornag.api.rest.bed

import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedMulched
import org.starbornag.api.domain.bed.CellStory
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Journal

/** How the journal says things: care as icons and words, plants by name, what grows in a line. */
object JournalWords {
    /** The care counted on a cell card, with its icon; planting is not care. */
    private val careIcons = listOf(
        BedCellWatered::class.java to "💧", BedFertilized::class.java to "🌿",
        BedMulched::class.java to "🪵", BedHarvested::class.java to "🧺"
    )

    fun grows(journal: Journal, line: Focus): String {
        val growing = journal.grows(line::covers)
            .map { (type, count) -> "$count ${type.replaceFirstChar { it.uppercase() }}" }
        val empty = journal.cells.count { line.covers(it.position) && it.planting.isEmpty() }
        return (growing + if (empty > 0) listOf("$empty empty") else emptyList()).joinToString(" · ")
    }

    fun plantName(story: CellStory): String = with(story.planting) {
        if (isEmpty()) "Empty"
        else plantType.replaceFirstChar { it.uppercase() } + if (plantCultivar.isEmpty()) "" else " · $plantCultivar"
    }

    fun careCounts(history: List<BedEvent>): String = careIcons.mapNotNull { (kind, icon) ->
        history.count { kind.isInstance(it) }.takeIf { it > 0 }?.let { "$icon$it" }
    }.joinToString(" ")

    fun icon(event: BedEvent): String = when (event) {
        is BedCellPlanted -> plantTypeToIcon(event.plantType).ifEmpty { "🌱" }
        is BedCellWatered -> "💧"
        is BedFertilized -> "🌿"
        is BedMulched -> "🪵"
        is BedHarvested -> "🧺"
    }

    fun word(event: BedEvent): String = when (event) {
        is BedCellPlanted -> "Planted"
        is BedCellWatered -> "Watered"
        is BedFertilized -> "Fed"
        is BedMulched -> "Mulched"
        is BedHarvested -> "Harvested"
    }
}
