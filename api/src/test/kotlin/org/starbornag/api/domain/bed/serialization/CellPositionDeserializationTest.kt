package org.starbornag.api.domain.bed.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.CellPosition

class CellPositionDeserializationTest : DeserializationTest<CellPosition>(CellPosition::class.java) {
    private val positionOne = CellPosition(1, 1)

    override val baseCases: Map<String, CellPosition>
        get() = listOf(
            jv("A1"),
            jv("A 1"),
            jv("A        1"),
            jv("A:1"),
            jv("1 1"),
            jv("1,1"),
            jv("1          1"),
            jv("1 . 1")
        ).associateWith { positionOne } + mapOf(
            jv("3,   5") to CellPosition(3, 5),
            jv("B6") to CellPosition(2, 6),
            jv("1.2") to CellPosition(1, 2),
            """{
                "row": 4,
                "column": 8
                }
            """.trimIndent() to CellPosition(4, 8)
        )

//    @Test
//    fun `deserializes object`() {
//        val input = ""
//        val actual = jacksonObjectMapper().readValue(input, type)
//        compare(actual, expected)
//    }
}