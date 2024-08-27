package org.starbornag.api.domain.bed

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator

import java.util.*

class BedCellAggregateStateTest {

    @Test
    fun `produces correct current state from historical events`(){
        val mapper = jacksonObjectMapper()
        val bedId = UUID.randomUUID()
        val cellId = UUID.randomUUID()

        val events = buildBedEvents(bedId, cellId) {
            planted(6.months.ago, "Tomato", "Dark Galaxy")
            watered(4.months.ago, 1.0)
            fertilized(2.months.ago, 1.0, "3-2-2 Vegan Mix")
            watered(5.days.ago, 2.0)
        }

        val state = BedCellAggregateState.fromEvents(events)

        val actual = mapper.writeValueAsString(state)

        val expected = """
            {
              "plantings": [
                {
                  "plantType": "Tomato",
                  "plantCultivar": "Dark Galaxy"
                }
              ],
              "watered": {
                "type": "watered",
                "bedId": "$bedId",
                "bedCellId": "$cellId",
                "started": 1724331019443,
                "volume": 2,
                "ended": 1724763019443
              },
              "fertilized": null,
              "mulched": null,
              "harvests": null
            }
        """.trimIndent()

        val doNotCareJustPass = { _: Any?, _: Any? -> true }

        JSONAssert.assertEquals(
            expected, actual,
            CustomComparator(JSONCompareMode.LENIENT,
                Customization("watered.started", doNotCareJustPass),
                Customization("watered.ended", doNotCareJustPass)
            )
        )
    }
}