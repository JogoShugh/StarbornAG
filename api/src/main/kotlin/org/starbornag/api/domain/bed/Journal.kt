package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.CellPosition
import java.util.*

/** One cell's story: what grows there and everything that happened to its soil, oldest first. */
data class CellStory(val position: CellPosition, val planting: Planting, val history: List<BedEvent>) {
    val latest: BedEvent? get() = history.maxByOrNull { it.started }
    val label: String get() = "${rowLetter(position.row)}${position.column}"
}

/**
 * One command as it reached the cells: a multi-cell command records one event per cell, all of the
 * same kind and at the same moment, so they fold back together.
 */
data class JournalCommand(val event: BedEvent, val positions: List<CellPosition>) {
    val started: Date get() = event.started

    /** The cells as spoken: "A1–A3" for a row, column or block, otherwise each cell, such as "A1 C4". */
    val spokenCells: String get() = spokenCells(positions)
}

/**
 * What happened across the cells of a focus, summed up the way a gardener reads it: each cell's
 * story with the most recently tended first, the bed's totals, and the commands behind the events.
 */
class Journal(stories: List<CellStory>) {
    val cells: List<CellStory> = stories.sortedWith(
        compareByDescending<CellStory> { it.latest?.started }
            .thenBy { it.position.row }
            .thenBy { it.position.column }
    )

    val planted: Int get() = cells.count { !it.planting.isEmpty() }
    val empty: Int get() = cells.size - planted
    val events: Int get() = cells.sumOf { it.history.size }
    val lastWatered: BedCellWatered?
        get() = cells.flatMap { it.history }.filterIsInstance<BedCellWatered>().maxByOrNull { it.started }

    /** The commands that reached the cells [within] the given ones, newest first. */
    fun commands(within: (CellPosition) -> Boolean = { true }): List<JournalCommand> =
        cells.filter { within(it.position) }
            .flatMap { story -> story.history.map { it to story.position } }
            .groupBy { (event, _) -> event.javaClass to event.started }
            .map { (_, reached) ->
                JournalCommand(reached.first().first, reached.map { it.second }.sortedWith(byPosition))
            }
            .sortedByDescending { it.started }

    /** What grows among the cells [within] the given ones, by plant type, in the order first seen. */
    fun grows(within: (CellPosition) -> Boolean): Map<String, Int> =
        cells.sortedWith(compareBy(byPosition) { it.position })
            .filter { within(it.position) && !it.planting.isEmpty() }
            .groupingBy { it.planting.plantType }
            .eachCount()

    private companion object {
        val byPosition: Comparator<CellPosition> = compareBy({ it.row }, { it.column })
    }
}

/**
 * Cells as spoken: a full rectangle of two or more cells is "first–last"; anything else is each
 * row's runs, such as "A1–A10 B1–B10 C1–C6" or "A1 A3".
 */
fun spokenCells(positions: List<CellPosition>): String {
    if (positions.isEmpty()) return ""
    val rows = positions.minOf { it.row }..positions.maxOf { it.row }
    val columns = positions.minOf { it.column }..positions.maxOf { it.column }
    val rectangle = positions.toSet().size == (rows.last - rows.first + 1) * (columns.last - columns.first + 1)
    return if (rectangle) {
        span(CellPosition(rows.first, columns.first), CellPosition(rows.last, columns.last))
    } else {
        positions.groupBy { it.row }.toSortedMap().flatMap { (row, inRow) ->
            runs(inRow.map { it.column }.sorted()).map { span(CellPosition(row, it.first), CellPosition(row, it.last)) }
        }.joinToString(" ")
    }
}

private fun label(position: CellPosition) = "${rowLetter(position.row)}${position.column}"

private fun span(first: CellPosition, last: CellPosition) =
    if (first == last) label(first) else "${label(first)}–${label(last)}"

/** Consecutive numbers as ranges: 1 2 3 5 is 1..3 and 5..5. */
private fun runs(numbers: List<Int>): List<IntRange> = numbers.fold(mutableListOf<IntRange>()) { found, n ->
    found.apply {
        if (isNotEmpty() && last().last == n - 1) set(lastIndex, last().first..n) else add(n..n)
    }
}
