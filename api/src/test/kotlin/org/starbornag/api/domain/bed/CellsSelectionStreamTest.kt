package org.starbornag.api.domain.bed

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellPositionList
import org.starbornag.api.domain.bed.command.CellRange
import org.starbornag.api.domain.bed.command.CellsSelection

class CellsSelectionStreamTest {

    private val rowCount = 4
    private val columnCount = 8

    @Test
    fun `streams single cell`() = runBlocking {
        val selection = CellsSelection(cell = CellPosition(2, 2))
        // ["B2"]
        val expected = listOf(CellPosition(2, 2))

        assert(streamCellPositions(selection), expected)
    }

    @Test
    fun `streams single row`() = runBlocking {
        val selection = CellsSelection(row = 1)
        // ["A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8"]
        val expected = (1..columnCount).map { CellPosition(1, it) }

        assert(streamCellPositions(selection), expected)
    }

    @Test
    fun `streams single column`() = runBlocking {
        val selection = CellsSelection(column = 3)
        // ["A3", "B3", "C3", "D3"]
        val expected = (1..rowCount).map { CellPosition(it, 3) }

        assert(streamCellPositions(selection), expected)
    }

    @Test
    fun `streams cell list`() = runBlocking {
        val selection = CellsSelection(cells = CellPositionList(
            CellPosition(1, 1),
            CellPosition(1, 3),
            CellPosition(2, 1),
            CellPosition(2, 3)
        ))
        // ["A1", "A3", "B1", "B3"]
        val expected = listOf(
            CellPosition(1, 1),
            CellPosition(1, 3),
            CellPosition(2, 1),
            CellPosition(2, 3)
        )

        assert(streamCellPositions(selection), expected)
    }

    @Test
    fun `streams native range`() = runBlocking {
        val selection = CellsSelection(cellRange = CellRange.of(1, 1, 3, 3))
        // ["A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"]
        val expected = (1..3).flatMap { row -> (1..3).map { col -> CellPosition(row, col) } }

        assert(streamCellPositions(selection), expected)
    }

    private suspend fun streamCellPositions(cellsSelection: CellsSelection) =
        cellsSelection.streamCellPositions(rowCount, columnCount)

    private fun assert(actual: Sequence<CellPosition>, expected: List<CellPosition>) =
        assertThat(actual.toList()).containsExactly(*expected.toTypedArray())
}