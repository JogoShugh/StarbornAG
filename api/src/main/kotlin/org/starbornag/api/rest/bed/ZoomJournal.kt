package org.starbornag.api.rest.bed

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.i
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.ol
import kotlinx.html.span
import org.starbornag.api.domain.bed.CellStory
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Journal
import org.starbornag.api.domain.bed.JournalCommand
import org.starbornag.api.rest.bed.BedPage.focusLink
import java.util.*

/**
 * The journal sheet: every cell's history under the focus, summed up by cell (the default), by row,
 * by column or as a timeline of the commands. Every card and line is a way in to its focus.
 */
object ZoomJournal {
    private val tabsAtTheBed = listOf("cell" to "Cells", "row" to "Rows", "column" to "Columns", "time" to "Timeline")
    private val tabsBelowIt = listOf("cell" to "Cells", "time" to "Timeline")

    fun FlowContent.journal(resource: FocusResource, focus: Focus, journal: Journal, choice: JournalChoice) {
        div {
            classes = setOf("sheet", "journal")
            attributes["data-by"] = choice.by
            controls(resource, focus, choice)
            div {
                classes = setOf("journal-head")
                h2 {
                    classes = setOf("sheet-title")
                    +"${if (focus == Focus.OnBed) resource.bedName else focus.label} journal"
                }
                span {
                    classes = setOf("sheet-meta")
                    +(if (focus == Focus.OnBed) "all ${journal.cells.size} cells" else "${journal.cells.size} cells")
                }
            }
            tabs(resource.bedId, focus, choice)
            when (choice.by) {
                "row", "column" -> lines(resource.bedId, journal, choice.by)
                "time" -> timeline(journal)
                else -> cells(resource.bedId, journal)
            }
        }
    }

    /** Half: Full or Close. Full: back to half; the Map control in the top bar does the same. */
    private fun FlowContent.controls(resource: FocusResource, focus: Focus, choice: JournalChoice) {
        div {
            classes = setOf("journal-controls")
            span { classes = setOf("grab") }
            if (choice.size == "full") {
                control("half", "▾ Half") { journalLink(resource.bedId, focus.path, choice.copy(size = "half")) }
            } else {
                control("full", "▴ Full") { journalLink(resource.bedId, focus.path, choice.copy(size = "full")) }
                control("close", "▾ Close") { focusLink(resource.bedId, focus.path) }
            }
        }
    }

    /** The top bar's way back to the map from a full journal. */
    fun FlowContent.mapControl(bedId: UUID, focus: Focus, choice: JournalChoice) =
        control("map", "🗺 Map") { journalLink(bedId, focus.path, choice.copy(size = "half")) }

    private fun FlowContent.tabs(bedId: UUID, focus: Focus, choice: JournalChoice) {
        nav {
            classes = setOf("tabs")
            (if (focus == Focus.OnBed) tabsAtTheBed else tabsBelowIt).forEach { (by, name) ->
                span {
                    classes = if (by == choice.by) setOf("tab", "on") else setOf("tab")
                    journalLink(bedId, focus.path, choice.copy(by = by))
                    +name
                }
            }
        }
    }

    private fun FlowContent.cells(bedId: UUID, journal: Journal) {
        div {
            classes = setOf("totals")
            total("planted", "${journal.planted}", "planted")
            total("empty", "${journal.empty}", "empty")
            total("events", "${journal.events}", "events")
            total("last-water", journal.lastWatered?.startedDescription ?: "never", "last water")
        }
        div {
            classes = setOf("cards")
            journal.cells.forEach { card(bedId, it) }
        }
    }

    private fun FlowContent.total(name: String, value: String, label: String) {
        div {
            attributes["data-total"] = name
            b { classes = setOf("value"); +value }
            span { +label }
        }
    }

    private fun FlowContent.card(bedId: UUID, story: CellStory) {
        div {
            classes = setOf("cell-card")
            attributes["data-cell"] = story.label
            focusLink(bedId, "cell/${story.label}")
            span { classes = setOf("chip"); +story.label }
            span { classes = setOf("card-icon"); +plantTypeToIcon(story.planting.plantType) }
            div {
                classes = setOf("card-body")
                div { classes = setOf("plant"); +JournalWords.plantName(story) }
                div {
                    classes = setOf("card-meta")
                    span { classes = setOf("care"); +JournalWords.careCounts(story.history) }
                    span {
                        classes = setOf("events")
                        +"${story.history.size} ${if (story.history.size == 1) "event" else "events"}"
                    }
                }
            }
            div {
                classes = setOf("last")
                story.latest?.let { +"${JournalWords.word(it).lowercase()} ${it.startedDescription}" }
            }
        }
    }

    private fun FlowContent.lines(bedId: UUID, journal: Journal, by: String) {
        div {
            classes = setOf("cards")
            journal.lines(by).forEach { line ->
                val cells = journal.cells.map { it.position }.filter(line::covers)
                    .sortedWith(compareBy({ it.row }, { it.column }))
                div {
                    classes = setOf("line-summary")
                    attributes["data-line"] = line.path
                    div {
                        classes = setOf("line-head")
                        focusLink(bedId, line.path)
                        b { +line.label }
                        span { classes = setOf("grows"); +JournalWords.grows(journal, line) }
                    }
                    ol {
                        journal.commands(line::covers).forEach { command ->
                            command(command) {
                                span {
                                    classes = setOf("strip")
                                    cells.forEach { i { if (it in command.positions) classes = setOf("on") } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.timeline(journal: Journal) {
        ol {
            classes = setOf("timeline", "cards")
            journal.commands().forEach { command(it) {} }
        }
    }

    private fun kotlinx.html.OL.command(command: JournalCommand, extra: FlowContent.() -> Unit) {
        li {
            classes = setOf("command")
            span { classes = setOf("what"); +"${JournalWords.icon(command.event)} ${JournalWords.word(command.event)}" }
            span { classes = setOf("cells"); +command.spokenCells }
            extra()
            span { classes = setOf("when"); +command.event.startedDescription }
        }
    }
}

/** Opens the journal at [choice]: the view is swapped and the address bar shows the journal. */
fun kotlinx.html.CommonAttributeGroupFacade.journalLink(bedId: UUID, focusPath: String, choice: JournalChoice) = hx {
    get = "/beds/$bedId/journal?${choice.query(focusPath)}"
    target = BedPage.VIEW
    swap = "outerHTML"
    pushUrl = true
}

/** A journal control (Full, Close, Half, Map), named for the steps and agents that tap it. */
private fun FlowContent.control(name: String, text: String, link: kotlinx.html.BUTTON.() -> Unit) {
    button(type = ButtonType.button) {
        classes = setOf("journal-control")
        attributes["data-control"] = name
        link()
        +text
    }
}
