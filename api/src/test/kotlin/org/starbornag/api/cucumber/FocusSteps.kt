package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.cucumber.java.ParameterType
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.starbornag.api.domain.bed.Focus
import org.starbornag.api.domain.bed.Move
import org.starbornag.api.domain.bed.command.CellPosition

class FocusSteps {

    private var focus: Focus = Focus.OnBed
    private var rows = 0
    private var columns = 0

    /** "cell B4", "row B", "column 4" or "the bed", as the feature files write a focus. */
    @ParameterType("cell [A-Za-z]+\\d+|row [A-Za-z]+|column \\d+|the bed")
    fun focus(text: String): Focus {
        val ref = text.substringAfter(" ")
        return when {
            text.startsWith("cell") -> Focus.OnCell(CellPosition.of(ref))
            text.startsWith("row") -> Focus.OnRow(ref.uppercase()[0] - 'A' + 1)
            text.startsWith("column") -> Focus.OnColumn(ref.toInt())
            else -> Focus.OnBed
        }
    }

    @Given("the focus is on {focus} in a bed of {int} rows and {int} columns")
    fun theFocusIsOn(focus: Focus, rows: Int, columns: Int) {
        this.focus = focus
        this.rows = rows
        this.columns = columns
    }

    @When("the focus moves {word}")
    fun theFocusMoves(move: String) {
        focus = focus.moves(rows, columns).getValue(Move.of(move))
    }

    @Then("the focus is on {focus}")
    fun theFocusIsNowOn(expected: Focus) {
        assertThat(focus).isEqualTo(expected)
    }

    @Then("the possible moves are {string}")
    fun thePossibleMovesAre(moves: String) {
        val possible = focus.moves(rows, columns).keys.map { it.word }
        assertThat(possible.sorted()).isEqualTo(moves.split(" ").filter { it.isNotEmpty() }.sorted())
    }

    @Then("the focus covers {int} cells, starting {string}")
    fun theFocusCovers(count: Int, firstCells: String) {
        val cells = focus.cells(rows, columns).map { "${it.row}:${it.column}" }
        val expectedStart = firstCells.split(" ")
        assertThat(cells.size).isEqualTo(count)
        assertThat(cells.take(expectedStart.size)).isEqualTo(expectedStart)
    }
}
