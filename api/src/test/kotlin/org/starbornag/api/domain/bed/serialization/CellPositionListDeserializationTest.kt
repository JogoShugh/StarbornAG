package org.starbornag.api.domain.bed.serialization

import org.starbornag.api.domain.bed.command.CellPositionList
import org.starbornag.api.domain.bed.command.CellPosition.Companion.of

class CellPositionListDeserializationTest : DeserializationTest<CellPositionList>(CellPositionList::class.java) {
    private val expected = CellPositionList(
        of("A1"),
        of("A2"),
        of("A3")
    )

    override val baseCases: Map<String, CellPositionList>
        get() = listOf(
            """["A1", "A2", "A3"]""",
            """["1 1", "1 2", "1 3"]""",
            """["A:1", "A:2", "A:3"]""",
            """["A1", "1 2", "A:3"]"""
        ).associateWith { expected }
}