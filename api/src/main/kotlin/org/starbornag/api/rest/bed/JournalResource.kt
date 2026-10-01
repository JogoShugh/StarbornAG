package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.CellStory
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Journal
import org.starbornag.api.domain.bed.JournalCommand
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.rowLetter
import java.util.*

/** How the journal is open: folded [by] cell, row, column or time, at [size] half or full. */
data class JournalChoice(val by: String = "cell", val size: String = "half") {
    fun query(focusPath: String) = "focus=$focusPath&by=$by&size=$size"

    companion object {
        private val folds = setOf("cell", "row", "column", "time")

        /** Anything unknown falls back to the defaults: by cell, half open. */
        fun of(by: String, size: String) =
            JournalChoice(by.takeIf { it in folds } ?: "cell", if (size == "full") "full" else "half")
    }
}

/** The cells of [focus], each with its planting and history, as a journal. */
fun BedResourceWithCurrentState.journal(focus: Focus): Journal {
    val columns = rows.firstOrNull()?.cells?.size ?: 0
    return Journal(focus.cells(rows.size, columns).map { position ->
        val cell = rows[position.row - 1].cells[position.column - 1]
        CellStory(position, cell.planting, cell.events)
    })
}

/** The rows or columns of the journal's cells, when folding [by] row or column. */
fun Journal.lines(by: String): List<Focus> = when (by) {
    "row" -> cells.map { it.position.row }.distinct().sorted().map { Focus.OnRow(it) }
    "column" -> cells.map { it.position.column }.distinct().sorted().map { Focus.OnColumn(it) }
    else -> emptyList()
}

/** Whether [position] lies in this focus. */
fun Focus.covers(position: CellPosition): Boolean = when (this) {
    is Focus.OnCell -> this.position == position
    is Focus.OnRow -> position.row == row
    is Focus.OnColumn -> position.column == column
    Focus.OnBed -> true
}

/** The event's name as the API writes it, such as "watered". */
val BedEvent.typeName: String get() = javaClass.getAnnotation(JsonTypeName::class.java)?.value ?: javaClass.simpleName

data class JournalEntry(val type: String, val cells: List<String>, val started: Date) {
    companion object {
        fun of(command: JournalCommand) = JournalEntry(
            command.event.typeName,
            command.positions.map { "${rowLetter(it.row)}${it.column}" },
            command.started
        )
    }
}

data class JournalCell(
    val cell: String,
    val plantType: String,
    val plantCultivar: String,
    val events: Int,
    val last: String?,
    val lastStarted: Date?,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>
)

data class JournalLine(
    val line: String,
    val grows: Map<String, Int>,
    val commands: List<JournalEntry>,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>
)

/**
 * The journal as HAL for agents: the same summary the page shows, with a link to the focus of every
 * cell and line, so an agent can move there and act with that focus's forms.
 */
data class JournalResource(
    val bedId: UUID,
    val focus: String,
    val by: String,
    val planted: Int,
    val empty: Int,
    val events: Int,
    val lastWatered: Date?,
    val cells: List<JournalCell>,
    val lines: List<JournalLine>,
    val timeline: List<JournalEntry>,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>
) {
    companion object {
        fun of(bedId: UUID, focus: Focus, by: String, journal: Journal): JournalResource {
            val base = "/beds/$bedId"
            fun link(path: String) = mapOf("focus" to mapOf("href" to "$base/focus/$path"))
            return JournalResource(
                bedId = bedId,
                focus = focus.path,
                by = by,
                planted = journal.planted,
                empty = journal.empty,
                events = journal.events,
                lastWatered = journal.lastWatered?.started,
                cells = journal.cells.map { it.toEntry(::link) },
                lines = journal.lines(by).map { line ->
                    JournalLine(
                        line.path,
                        journal.grows(line::covers),
                        journal.commands(line::covers).map(JournalEntry::of),
                        link(line.path)
                    )
                },
                timeline = journal.commands().map(JournalEntry::of),
                links = mapOf(
                    "self" to mapOf("href" to "$base/journal?focus=${focus.path}&by=$by"),
                    "focus" to mapOf("href" to "$base/focus/${focus.path}")
                ) + folds(focus).associate { fold ->
                    "by-$fold" to mapOf("href" to "$base/journal?focus=${focus.path}&by=$fold")
                }
            )
        }

        /** By row and by column only make sense across the whole bed. */
        private fun folds(focus: Focus) =
            if (focus == Focus.OnBed) listOf("cell", "row", "column", "time") else listOf("cell", "time")

        private fun CellStory.toEntry(link: (String) -> Map<String, Map<String, String>>) = JournalCell(
            label, planting.plantType, planting.plantCultivar, history.size,
            latest?.typeName, latest?.started, link("cell/$label")
        )
    }
}
