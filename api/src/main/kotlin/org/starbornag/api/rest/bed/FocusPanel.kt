package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.unsafe

/**
 * Where the gardener stands, rendered from the same FocusResource agents read as JSON: the pad is
 * built from the move links (a missing arrow is an edge of the bed) and the action bar from the
 * forms, asking only for what the care needs.
 */
object FocusPanel {
    const val TARGET = "#focus-panel"

    private val careLabels = mapOf(
        "plant" to "🌱 Plant", "water" to "💧 Water", "fertilize" to "🌿 Fertilize",
        "mulch" to "🪵 Mulch", "harvest" to "🧺 Harvest"
    )
    private val arrows = mapOf(
        "northwest" to "↖", "north" to "↑", "northeast" to "↗", "west" to "←", "east" to "→",
        "southwest" to "↙", "south" to "↓", "southeast" to "↘"
    )

    /** Pad slots by focus scope: a compass for a cell, a line for a row or a column, none for the bed. */
    private val padShapes = mapOf(
        "cell" to ("compass" to listOf(
            "northwest", "north", "northeast", "west", "zoom-out", "east", "southwest", "south", "southeast"
        )),
        "row" to ("vertical" to listOf("north", "zoom-out", "south")),
        "column" to ("horizontal" to listOf("west", "zoom-out", "east")),
        "bed" to ("empty" to emptyList())
    )
    private val filledByThePage = setOf("bedId", "started", "location")

    /** What htmx swaps in when the focus moves: the panel, plus the highlight out of band. */
    fun fragment(focus: FocusResource, message: String? = null): String =
        unwrap(createHTML().div { panel(focus, message) }) +
            unwrap(createHTML().div { highlight(focus, outOfBand = true) })

    fun FlowContent.panel(focus: FocusResource, message: String?) {
        div {
            id = "focus-panel"
            h2 {
                classes = setOf("focus-label")
                +focus.focus
            }
            p {
                classes = setOf("focus-summary")
                +summary(focus)
            }
            if (message != null) p {
                classes = setOf("focus-message")
                +message
            }
            pad(focus)
            actionBar(focus)
        }
    }

    /** Highlights the cells in focus without touching the live grid. */
    fun FlowContent.highlight(focus: FocusResource, outOfBand: Boolean) {
        val selector =
            if (focus.path == "bed") ".grid-item" else focus.cells.joinToString(", ") { "#cell-${it.position}" }
        style {
            id = "focus-style"
            if (outOfBand) attributes["hx-swap-oob"] = "true"
            unsafe { +"$selector { outline: 4px solid gold; outline-offset: -4px; }" }
        }
    }

    private fun FlowContent.pad(focus: FocusResource) {
        val (shape, slots) = padShapes.getValue(focus.path.substringBefore("/"))
        div {
            classes = setOf("pad")
            attributes["data-shape"] = shape
            slots.forEach { move ->
                val link = focus.links[move]
                if (link == null) {
                    span { classes = setOf("pad-empty") }
                } else {
                    button {
                        classes = setOf("pad-move")
                        attributes["data-move"] = move
                        attributes["aria-label"] = "$move to ${link["title"]}"
                        hx {
                            get = link.getValue("href").replaceFirst("/api/beds/", "/beds/")
                            target = TARGET
                            swap = "outerHTML"
                            pushUrl = true
                        }
                        +(arrows[move] ?: "Out to ${link["title"]}")
                    }
                }
            }
        }
    }

    private fun FlowContent.actionBar(focus: FocusResource) {
        div {
            classes = setOf("action-bar")
            focus.forms.values.forEach { careForm ->
                val action = careForm.links.getValue("target").getValue("href").substringAfterLast("/")
                form {
                    classes = setOf("care-action")
                    attributes["data-action"] = action
                    hx {
                        post = "/beds/${focus.bedId}/focus/${focus.path}/$action"
                        target = TARGET
                        swap = "outerHTML"
                    }
                    val properties = careForm.schema.get("properties")
                    careForm.schema.get("required").map { it.asText() }
                        .filter { it !in filledByThePage }
                        .forEach { field(it, properties.get(it)) }
                    button(type = ButtonType.submit) { +(careLabels[action] ?: action) }
                }
            }
        }
    }

    private fun FlowContent.field(name: String, schema: JsonNode) {
        when {
            schema.has("enum") -> select {
                this.name = name
                schema.get("enum").forEach { option { +it.asText() } }
            }
            schema.get("type")?.asText() in setOf("number", "integer") ->
                input(type = InputType.number, name = name) {
                    attributes["step"] = "any"
                    value = "1"
                }
            else -> input(type = InputType.text, name = name) { placeholder = name }
        }
    }

    private fun summary(focus: FocusResource): String {
        val planted = focus.cells.filter { it.planting.plantType.isNotEmpty() }
        return when {
            focus.cells.size == 1 && planted.isNotEmpty() -> "Growing ${planted.single().planting}"
            focus.cells.size == 1 -> "Empty"
            else -> "${focus.cells.size} cells, ${planted.size} planted"
        }
    }

    private fun unwrap(html: String): String = html.trim().removePrefix("<div>").removeSuffix("</div>")
}
