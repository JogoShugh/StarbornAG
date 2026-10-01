package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import org.starbornag.api.rest.bed.BedPage.focusLink
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.details
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.ol
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.summary
import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.BedEvent
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedMulched
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Move

/**
 * The details of where the gardener stands, rendered from the same FocusResource agents read as JSON:
 * the breadcrumb follows the zoom-out moves back to the whole bed, and the care buttons come from the
 * forms, asking only for what the care needs.
 */
object ZoomSheet {
    private val careLabels = mapOf(
        "plant" to "🌱 Plant", "water" to "💧 Water", "fertilize" to "🌿 Feed",
        "mulch" to "🪵 Mulch", "harvest" to "🧺 Harvest"
    )
    private val filledByThePage = setOf("bedId", "started", "location")

    /** The way back out, from the whole bed down to [focus]; every crumb but the last is a tap target. */
    fun FlowContent.crumbs(resource: FocusResource, focus: Focus, rows: Int, columns: Int) {
        val trail = generateSequence(focus) { it.moves(rows, columns)[Move.ZOOM_OUT] }
            .toList().reversed()
        nav {
            classes = setOf("crumbs")
            trail.forEachIndexed { index, step ->
                if (index > 0) span { classes = setOf("crumb-separator"); +"›" }
                span {
                    classes = if (step == focus) setOf("crumb", "here") else setOf("crumb")
                    focusLink(resource.bedId, step.path)
                    +(if (step == Focus.OnBed) resource.bedName else step.label)
                }
            }
        }
    }

    fun FlowContent.themeToggle() {
        button(type = ButtonType.button) {
            classes = setOf("theme-toggle")
            attributes["onclick"] = "starbornToggleTheme()"
            attributes["aria-label"] = "Switch between light and dark"
            +"◐"
        }
    }

    /** [history] is the cell's own history, oldest first, for a cell view; null for wider views. */
    fun FlowContent.sheet(resource: FocusResource, message: String?, history: List<BedEvent>?) {
        div {
            classes = setOf("sheet")
            button(type = ButtonType.button) {
                classes = setOf("handle")
                attributes["aria-label"] = "Open the journal"
                journalLink(resource.bedId, resource.path, JournalChoice())
                span { classes = setOf("grab") }
                +"Journal ▴"
            }
            h2 {
                classes = setOf("sheet-title")
                +title(resource)
            }
            p {
                classes = setOf("sheet-meta")
                +summary(resource)
            }
            if (message != null) p {
                classes = setOf("sheet-message")
                +message
            }
            careActions(resource)
            if (history != null) history(history)
        }
    }

    /** What happened to the soil, newest first, with how long ago. */
    private fun FlowContent.history(history: List<BedEvent>) {
        if (history.isEmpty()) {
            p {
                classes = setOf("history", "empty")
                +"Nothing yet"
            }
            return
        }
        ol {
            classes = setOf("history")
            history.asReversed().forEach { event ->
                li {
                    span { classes = setOf("what"); +describe(event) }
                    span { classes = setOf("when"); +event.startedDescription }
                }
            }
        }
    }

    private fun describe(event: BedEvent): String = when (event) {
        is BedCellPlanted -> "${plantTypeToIcon(event.plantType)} Planted ${event.plantType}".trim() +
            cultivar(event.plantCultivar)
        is BedCellWatered -> "💧 Watered"
        is BedFertilized -> "🌿 Fed ${event.fertilizer}".trim()
        is BedMulched -> "🪵 Mulched with ${event.material}".removeSuffix(" with ")
        is BedHarvested -> "🧺 Harvested ${event.plantType}" + cultivar(event.plantCultivar)
    }

    private fun cultivar(name: String) = if (name.isEmpty()) "" else " · $name"

    private fun FlowContent.careActions(resource: FocusResource) {
        div {
            classes = setOf("care-actions")
            resource.forms.values.filter { it.isCare }.forEach { careForm ->
                val postTo = careForm.target
                val action = postTo.substringAfterLast("/")
                val properties = careForm.schema.get("properties")
                val fields = careForm.schema.get("required").map { it.asText() }.filter { it !in filledByThePage }
                form {
                    classes = setOf("care-action")
                    attributes["data-action"] = action
                    hx {
                        post = postTo
                        target = BedPage.VIEW
                        swap = "outerHTML"
                    }
                    val label = careLabels[action] ?: action
                    if (fields.isEmpty()) {
                        button(type = ButtonType.submit) { +label }
                    } else {
                        details {
                            summary { +label }
                            fields.forEach { field(it, properties.get(it)) }
                            button(type = ButtonType.submit) { +"Record" }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.field(name: String, schema: JsonNode) {
        when {
            schema.has("enum") -> select {
                this.name = name
                attributes["aria-label"] = name
                schema.get("enum").forEach { option { +it.asText() } }
            }
            schema.get("type")?.asText() in setOf("number", "integer") ->
                input(type = InputType.number, name = name) {
                    attributes["step"] = "any"
                    attributes["aria-label"] = name
                    value = "1"
                }
            else -> input(type = InputType.text, name = name) { placeholder = name }
        }
    }

    private fun title(resource: FocusResource): String {
        val single = resource.cells.singleOrNull()
        return when {
            resource.path == "bed" -> resource.bedName
            single != null && single.planting.plantType.isNotEmpty() ->
                with(single.planting) {
                    val name = plantType.replaceFirstChar { it.uppercase() }
                    "${plantTypeToIcon(plantType)} $name".trim() + cultivar(plantCultivar)
                }
            else -> resource.focus
        }
    }

    private fun summary(resource: FocusResource): String {
        val planted = resource.cells.count { it.planting.plantType.isNotEmpty() }
        return when (resource.cells.size) {
            1 -> if (planted == 1) "${resource.focus} · tap a neighbor to step there" else "${resource.focus} · empty"
            else -> "$planted of ${resource.cells.size} planted"
        }
    }
}
