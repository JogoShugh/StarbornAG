package org.starbornag.api.domain.bed.serialization

import org.starbornag.api.domain.bed.command.CellRange

class CellRangeDeserializationTest : DeserializationTest<CellRange>(CellRange::class.java) {
    private val expectedCellRange = CellRange.of("A1", "B3")
    override val baseCases: Map<String, CellRange>
        get() = sequenceOf(
            jv("A1 to B3"),
            jv("A:1 through B:3"),
            jv("A 1 , B 3"),
            jv("1 1 - 2 3"),
            jv("1.1 until 2.3")
        ).associateWith {expectedCellRange }
}