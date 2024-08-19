package org.starbornag.api.domain.bed.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellPositionList
import org.starbornag.api.domain.bed.command.CellsSelection
import org.starbornag.api.domain.bed.command.CellPosition.Companion.of
import org.starbornag.api.domain.bed.command.CellRange

class CellsSelectionDeserializerTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes single cell reference string`() {
        val input = "\"A1\""
        val expected = CellsSelection(cell = of("A1"))
        val actual = mapper.readValue(input, CellsSelection::class.java)
        assertEqual(actual, expected)
    }

    @Test
    fun `deserializes cell range string`() {
        val input = "\"A1 to B2\""
        val expected = CellsSelection(cellRange = CellRange.of("A1", "B2"))
        val actual = mapper.readValue(input, CellsSelection::class.java)
        assertEqual(actual, expected)
    }

    @Test
    fun `deserializes cells = A1, A2, A3`() {
        val input = """
            {
                "cells": ["A1", "A2", "A3"]            
            }            
        """.trimIndent()

        val actual = mapper.readValue(input, CellsSelection::class.java)
        val expected = CellsSelection(cells = CellPositionList(
                of("A1"),
                of("A2"),
                of("A3")
            )
        )

        assertEqual(actual, expected)
    }

    @Test
    fun `deserializes maximally structured json`() {
        val input = """
            {
                "row": 1,
                "column": 1,
                "cell": {"row":1, "column":1},
                "cells": ["A1", "A2", "A3"],
                "cellRange": {"cellStart":{"row":1, "column":1}, "cellEnd": {"row":2,"column":2}},
                "cellStart": {"row":1,"column":1},
                "cellEnd": {"row":2,"column":2}                         
            }            
        """.trimIndent()

        val actual = mapper.readValue(input, CellsSelection::class.java)
        val expected = CellsSelection(
            row = 1,
            column = 1,
            cell = CellPosition(1, 1),
            cells = CellPositionList(
                of("A1"),
                of("A2"),
                of("A3")
            ),
            cellRange = CellRange.of(1, 1, 2, 2),
            cellStart = CellPosition(1, 1),
            cellEnd = CellPosition(2, 2)
        )

        assertEqual(actual, expected)
    }

    @Test
    fun `deserializes maximally stringy json`() {
        // Note Jackson automatically knows how to convert "1" to 1, etc:
        // Note: for Cells, for deserialization simplicity, we cannot
        // have "1 1 1 2 1 3" -- an admittedly odd case,
        // but theoretically possible from bad AI translations.
        //
        // If it becomes a problem, we can adjust it...
        val input = """
            {
                "row": " 1 ",
                "column": " 1   ",
                "cell": "A 1",
                "cells": "A1 1:2, A:3",
                "cellRange": "A 1 to B 2",
                "cellStart": "1 1",
                "cellEnd": "2 : 2"                         
            }            
        """.trimIndent()

        val actual = mapper.readValue(input, CellsSelection::class.java)
        val expected = CellsSelection(
            row = 1,
            column = 1,
            cell = CellPosition(1, 1),
            cells = CellPositionList(
                of("A1"),
                of("A2"),
                of("A3")
            ),
            cellRange = CellRange.of(1, 1, 2, 2),
            cellStart = CellPosition(1, 1),
            cellEnd = CellPosition(2, 2)
        )

        assertEqual(actual, expected)
    }

    private fun assertEqual(
        actual: CellsSelection,
        expected: CellsSelection
    ) {
        // TODO there's gotta be cleaner way...
        val actJson = mapper.writeValueAsString(actual)
        val expJson = mapper.writeValueAsString(expected)
        assertThat(actJson).isEqualTo(expJson)
    }
}