package org.starbornag.api.rest.bed

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.unsafe
import org.starbornag.api.domain.bed.rowLetter
import java.util.*

/**
 * The bed page: the live grid with tappable row letters, column numbers and cells, and the focus
 * panel (see FocusPanel). The grid is rendered once and stays connected to the SSE announcements;
 * moving the focus swaps only the panel and the highlight.
 */
object BedPage {
    private const val CELL_WIDTH_PX = 80
    private const val LABEL_WIDTH_PX = 40

    fun page(bed: BedResourceWithCurrentState, focus: FocusResource): String = createHTML().html {
        head {
            meta(charset = "UTF-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1")
            title { +bed.name }
            script(src = "https://unpkg.com/htmx.org@2.0.2") {}
            script(src = "https://unpkg.com/htmx-ext-sse@2.2.2/sse.js") {}
            script(src = "//cdnjs.cloudflare.com/ajax/libs/annyang/2.6.1/annyang.min.js") {}
            script(src = "//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/speechkitt.min.js") {}
            script { unsafe { +voiceScript(bed.id) } }
            script(src = "/script.js") {}
            link(rel = "stylesheet", href = "/styles.css")
        }
        body {
            id = "garden"
            div {
                classes = setOf("centered-container")
                h1 {
                    classes = setOf("garden-name")
                    focusLink(bed.id, "bed")
                    +bed.name
                }
                grid(bed)
                with(FocusPanel) {
                    highlight(focus, outOfBand = false)
                    panel(focus, message = null)
                }
            }
        }
    }

    private fun FlowContent.grid(bed: BedResourceWithCurrentState) {
        val columns = bed.rows.firstOrNull()?.cells?.size ?: 0
        div {
            id = "bed-${bed.id}"
            classes = setOf("grid-container", "bed-grid")
            hx {
                ext = "sse"
                sseConnect = "/api/beds/${bed.id}/events?clientId=${UUID.randomUUID()}"
            }
            // Cells share the width, so a wide bed shrinks to fit a phone instead of overflowing it.
            attributes["style"] = "grid-template-columns: auto repeat($columns, minmax(0, 1fr)); " +
                "max-width: ${columns * CELL_WIDTH_PX + LABEL_WIDTH_PX}px;"
            div { classes = setOf("grid-corner") }
            for (column in 1..columns) {
                div {
                    classes = setOf("col-label")
                    attributes["data-column"] = "$column"
                    focusLink(bed.id, "column/$column")
                    +"$column"
                }
            }
            bed.rows.forEachIndexed { rowIndex, row ->
                val letter = rowLetter(rowIndex + 1)
                div {
                    classes = setOf("row-label")
                    attributes["data-row"] = letter
                    focusLink(bed.id, "row/$letter")
                    +letter
                }
                row.cells.forEachIndexed { columnIndex, cell -> cell(bed.id, "$letter${columnIndex + 1}", cell) }
            }
        }
    }

    private fun FlowContent.cell(bedId: UUID, label: String, cell: BedResourceCell) {
        div {
            id = "cell-$label"
            classes = setOf("grid-item")
            focusLink(bedId, "cell/$label")
            div {
                classes = setOf("plant")
                span {
                    classes = setOf("plant-icon", "large-icon")
                    hx {
                        sseSwap = "plants-${cell.bedCellId}"
                        target = "this"
                    }
                    +plantTypeToIcon(cell.planting.plantType)
                }
            }
            div {
                classes = setOf("events")
                hx {
                    sseSwap = "events-${cell.bedCellId}"
                    swap = "beforeend"
                    target = "this"
                }
                cell.events.forEach { span { +iconMap.getOrDefault(it.javaClass.simpleName, "") } }
            }
        }
    }

    /** Tapping moves the focus: the panel is swapped and the address bar shows the focus. */
    fun CommonAttributeGroupFacade.focusLink(bedId: UUID, path: String) = hx {
        get = "/beds/$bedId/focus/$path"
        target = FocusPanel.TARGET
        swap = "outerHTML"
        pushUrl = true
    }

    /** Free speech after "starborn" still goes to the AI; the fixed navigation grammar comes next. */
    private fun voiceScript(bedId: UUID) = """
        function starbornSend(entry) {
          fetch('/api/beds/$bedId/ai/plant', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'prompt=' + encodeURIComponent(entry)
          });
        }
        document.addEventListener('DOMContentLoaded', function () {
          if (window.annyang) {
            annyang.addCommands({ 'starborn *entry': starbornSend });
            SpeechKITT.annyang();
            SpeechKITT.setInstructionsText('Example: Starborn, watered row 1.');
            SpeechKITT.setStylesheet('//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/themes/flat-pumpkin.css');
            SpeechKITT.vroom();
          }
        });
    """.trimIndent()
}
