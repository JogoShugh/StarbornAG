package org.starbornag.api.rest.bed

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import org.starbornag.api.rest.bed.BedPage.focusLink
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.span
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Move
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.rowLetter
import java.util.*

/**
 * The map part of a zoom view: the whole bed, one row or column with its neighbors peeking in, or one
 * cell among its eight neighbors. Rows are letters, columns numbers.
 *
 * Every placed element carries its grid position for both orientations as CSS variables:
 * --lr/--lc in landscape (letters down the side, numbers across) and --pr/--pc in portrait, where the
 * bed is turned to fit a tall screen (letters across the top, numbers down the side).
 */
object ZoomMap {
    /** Row and column offsets of the neighborhood's slots, northwest to southeast. */
    private val compass = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 0, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    private const val RECENT_CARE_ICONS = 4

    /** The bed's cells by position, with the bed's size. */
    class Layout(bed: BedResourceWithCurrentState) {
        val bedId: UUID = bed.id
        val rows: Int = bed.rows.size
        val columns: Int = bed.rows.firstOrNull()?.cells?.size ?: 0
        private val cells = bed.rows.map { it.cells }

        /** The cell at [position], or null past the bed's edge. */
        fun cell(position: CellPosition): BedResourceCell? =
            cells.getOrNull(position.row - 1)?.getOrNull(position.column - 1)
    }

    fun FlowContent.bedMap(layout: Layout) {
        div {
            classes = setOf("bed-map")
            attributes["style"] = "--rows: ${layout.rows}; --columns: ${layout.columns};"
            div { classes = setOf("corner"); place(1, 1) }
            for (column in 1..layout.columns) {
                div {
                    classes = setOf("col-label")
                    attributes["data-column"] = "$column"
                    place(1, column + 1)
                    focusLink(layout.bedId, "column/$column")
                    +"$column"
                }
            }
            for (row in 1..layout.rows) {
                div {
                    classes = setOf("row-label")
                    attributes["data-row"] = rowLetter(row)
                    place(row + 1, 1)
                    focusLink(layout.bedId, "row/${rowLetter(row)}")
                    +rowLetter(row)
                }
                for (column in 1..layout.columns) {
                    tile(layout, CellPosition(row, column), setOf("tile")) { place(row + 1, column + 1) }
                }
            }
        }
    }

    /**
     * A row or a column in focus, with the one before and after peeking in where they really are;
     * past the bed's edge the peek is hatched.
     */
    fun FlowContent.lines(layout: Layout, focus: Focus) {
        val (axis, before, after) = when (focus) {
            is Focus.OnRow -> Triple("row", Move.NORTH, Move.SOUTH)
            else -> Triple("column", Move.WEST, Move.EAST)
        }
        val moves = focus.moves(layout.rows, layout.columns)
        div {
            classes = setOf("lines")
            attributes["data-axis"] = axis
            peek(layout, "before", moves[before])
            div {
                classes = setOf("line", "in-focus")
                focus.cells(layout.rows, layout.columns).forEach { tile(layout, it, setOf("tile")) {} }
            }
            peek(layout, "after", moves[after])
        }
    }

    private fun FlowContent.peek(layout: Layout, side: String, next: Focus?) {
        div {
            classes = if (next == null) setOf("peek", "edge") else setOf("peek")
            attributes["data-side"] = side
            if (next != null) {
                focusLink(layout.bedId, next.path)
                attributes["aria-label"] = next.label
                next.cells(layout.rows, layout.columns).forEach { position ->
                    span {
                        classes = setOf("peek-tile")
                        +plantTypeToIcon(layout.cell(position)?.planting?.plantType.orEmpty())
                    }
                }
            }
        }
    }

    /** The cell in the middle of its eight neighbors, in compass order; tapping a neighbor steps onto it. */
    fun FlowContent.neighborhood(layout: Layout, center: CellPosition) {
        div {
            classes = setOf("hood")
            compass.forEach { (dRow, dColumn) ->
                val position = CellPosition(center.row + dRow, center.column + dColumn)
                val placement: DIV.() -> Unit = { place(2 + dRow, 2 + dColumn) }
                when {
                    layout.cell(position) == null -> div { classes = setOf("slot", "edge"); placement() }
                    position == center -> tile(layout, position, setOf("tile", "slot", "center"), false, placement)
                    else -> tile(layout, position, setOf("tile", "slot"), true, placement)
                }
            }
        }
    }

    private fun FlowContent.tile(
        layout: Layout,
        position: CellPosition,
        tileClasses: Set<String>,
        tappable: Boolean = true,
        placement: DIV.() -> Unit
    ) {
        val cell = layout.cell(position)!!
        val label = "${rowLetter(position.row)}${position.column}"
        div {
            classes = tileClasses
            attributes["data-cell"] = label
            placement()
            if (tappable) focusLink(layout.bedId, "cell/$label")
            span { classes = setOf("tile-label"); +label }
            span {
                classes = setOf("plant-icon")
                hx {
                    sseSwap = "plants-${cell.bedCellId}"
                    target = "this"
                }
                +plantTypeToIcon(cell.planting.plantType)
            }
            span {
                classes = setOf("events")
                hx {
                    sseSwap = "events-${cell.bedCellId}"
                    swap = "beforeend"
                    target = "this"
                }
                cell.events.takeLast(RECENT_CARE_ICONS)
                    .forEach { span { +iconMap.getOrDefault(it.javaClass.simpleName, "") } }
            }
        }
    }

    /**
     * Places the element at the bed's [row] and [column] on a grid: as is in landscape (--lr/--lc),
     * turned in portrait (--pr/--pc) so the row letters run across the top.
     */
    private fun DIV.place(row: Int, column: Int) {
        attributes["style"] = attributes["style"].orEmpty() +
            "--lr: $row; --lc: $column; --pr: $column; --pc: $row;"
    }
}
