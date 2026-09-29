package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellsSelection

class LocationSteps {

    private var selectedCells: List<CellPosition> = emptyList()

    @When("the location {string} is resolved in a bed of {int} rows and {int} columns")
    fun theLocationIsResolved(location: String, rows: Int, columns: Int) = runBlocking {
        selectedCells = CellsSelection.fromString(location).streamCellPositions(rows, columns).toList()
    }

    /** Cells written as "row:column", separated by spaces. */
    @Then("the selected cells are {string}")
    fun theSelectedCellsAre(cells: String) {
        val expected = cells.split(" ").map { cell ->
            val (row, column) = cell.split(":").map(String::toInt)
            CellPosition(row, column)
        }
        assertThat(selectedCells).isEqualTo(expected)
    }
}
