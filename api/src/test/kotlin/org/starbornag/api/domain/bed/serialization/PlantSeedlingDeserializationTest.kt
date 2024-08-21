package org.starbornag.api.domain.bed.serialization

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.starbornag.api.domain.bed.command.*
import java.time.Instant
import java.util.*

class PlantSeedlingDeserializationTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserializes command with location`() {
        val id = UUID.randomUUID()
        val date = Date.from(Instant.now())
        val plantType = "Tomato"
        val plantCultivar = "Dark Galaxy"

        val expected = BedCommand.CellCommand.PlantSeedling(
            id,
            date,
            plantType,
            plantCultivar,
            location = CellsSelection.fromString("A1, A4")
        )

        val input = """
            {
                "bedId": "${id.toString()}",
                "started": "${mapper.writeValueAsString(date)}",
                "plantType": "$plantType",
                "plantCultivar": "$plantCultivar",
                "location": "A1, A4"
            }           
        """.trimIndent()

        val actual = mapper.readValue(input, BedCommand.CellCommand.PlantSeedling::class.java)
        assertEqual(actual, expected)
    }

    private fun assertEqual(
        actual: BedCommand.CellCommand.PlantSeedling,
        expected: BedCommand.CellCommand.PlantSeedling
    ) {
        // TODO there's gotta be cleaner way...
        val actJson = mapper.writeValueAsString(actual)
        val expJson = mapper.writeValueAsString(expected)
        assertThat(actJson).isEqualTo(expJson)
    }
}