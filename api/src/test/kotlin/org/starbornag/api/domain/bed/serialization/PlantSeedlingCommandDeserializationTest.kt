package org.starbornag.api.domain.bed.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellPositionList
import org.starbornag.api.domain.bed.command.CellsSelection
import org.starbornag.api.domain.bed.command.CellPosition.Companion.of
import org.starbornag.api.domain.bed.command.CellRange

class PlantSeedlingCommandDeserializationTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes command with location`() {

        val input = "\"A1\""
        val expected = CellsSelection(cell = CellPosition.of("A1"))
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