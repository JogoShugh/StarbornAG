package org.starbornag.api.rest.bed

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.unsafe
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.rest.bed.ZoomMap.bedMap
import org.starbornag.api.rest.bed.ZoomMap.lines
import org.starbornag.api.rest.bed.ZoomMap.neighborhood
import org.starbornag.api.rest.bed.ZoomSheet.crumbs
import org.starbornag.api.rest.bed.ZoomSheet.sheet
import org.starbornag.api.rest.bed.ZoomSheet.themeToggle
import java.util.*

/**
 * The bed page: a shell that stays connected to the SSE announcements around the view, which IS the
 * focus. The view shows the whole bed, one row, one column or one cell among its neighbors (see
 * ZoomMap), with the details and care in a sheet (see ZoomSheet). Moving the focus swaps only the view.
 */
object BedPage {
    const val VIEW = "#view"

    fun page(bed: BedResourceWithCurrentState, focus: FocusResource, message: String? = null): String =
        createHTML().html {
            lang = "en"
            attributes["data-theme"] = "auto"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")
                title { +bed.name }
                script { unsafe { +THEME_SCRIPT } }
                script(src = "https://unpkg.com/htmx.org@2.0.2") {}
                script(src = "https://unpkg.com/htmx-ext-sse@2.2.2/sse.js") {}
                script(src = "//cdnjs.cloudflare.com/ajax/libs/annyang/2.6.1/annyang.min.js") {}
                script(src = "//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/speechkitt.min.js") {}
                script { unsafe { +voiceScript(bed.id) } }
                link(rel = "stylesheet", href = "/styles.css")
            }
            body {
                div {
                    classes = setOf("shell")
                    hx {
                        ext = "sse"
                        sseConnect = "/api/beds/${bed.id}/events?clientId=${UUID.randomUUID()}"
                    }
                    view(bed, focus, message)
                }
            }
        }

    /** What htmx swaps in when the focus moves or care is recorded: only the view. */
    fun fragment(bed: BedResourceWithCurrentState, focus: FocusResource, message: String? = null): String =
        createHTML().div { view(bed, focus, message) }.trim().removePrefix("<div>").removeSuffix("</div>")

    private fun FlowContent.view(bed: BedResourceWithCurrentState, resource: FocusResource, message: String?) {
        val layout = ZoomMap.Layout(bed)
        val focus = Focus.fromPath(resource.path, layout.rows, layout.columns)
        div {
            id = "view"
            attributes["data-focus"] = resource.path
            attributes["data-zoom"] = resource.path.substringBefore("/")
            div {
                classes = setOf("map-area")
                div {
                    classes = setOf("topbar")
                    crumbs(resource, focus, layout.rows, layout.columns)
                    themeToggle()
                }
                div {
                    classes = setOf("map")
                    when (focus) {
                        is Focus.OnCell -> neighborhood(layout, focus.position)
                        is Focus.OnRow, is Focus.OnColumn -> lines(layout, focus)
                        Focus.OnBed -> bedMap(layout)
                    }
                }
            }
            sheet(resource, message, (focus as? Focus.OnCell)?.let { layout.cell(it.position)?.events })
        }
    }

    /** Tapping moves the focus: the view is swapped and the address bar shows the focus. */
    fun CommonAttributeGroupFacade.focusLink(bedId: UUID, path: String) = hx {
        get = "/beds/$bedId/focus/$path"
        target = VIEW
        swap = "outerHTML"
        pushUrl = true
    }

    /** Follows the phone's light or dark setting until the gardener picks one, which is remembered. */
    private val THEME_SCRIPT = """
        (function () {
          try {
            var chosen = localStorage.getItem('theme');
            if (chosen) document.documentElement.dataset.theme = chosen;
          } catch (e) {}
        })();
        function starbornToggleTheme() {
          var root = document.documentElement;
          var now = root.dataset.theme;
          if (now !== 'light' && now !== 'dark') {
            now = matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
          }
          var next = now === 'light' ? 'dark' : 'light';
          root.dataset.theme = next;
          try { localStorage.setItem('theme', next); } catch (e) {}
        }
    """.trimIndent()

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
