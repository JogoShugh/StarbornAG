package org.starbornag.api.domain.bed

import com.eventstore.dbclient.EventData
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.util.*

class EventStoreDBRepositoryTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `it saves events to stream`() {
        val repo = EventStoreDBRepository()

        val bedId = UUID.randomUUID()
        val cellId = UUID.randomUUID()

        val streamName = "cellAggregate-$bedId-$cellId"

        val events = buildBedEvents(bedId, cellId) {
            planted(6.months.ago, "Tomato", "Dark Galaxy")
            watered(4.months.ago, 1.0)
            fertilized(2.months.ago, 1.0, "3-2-2 Vegan Mix")
            watered(5.days.ago, 2.0)
        }

        repo.append(streamName, events.map {
            val eventType = getEventType(it)
            EventData.builderAsJson(UUID.randomUUID(), eventType, it).build()
        }.asIterable(), 1)

        val eventsFromStream = repo.fetch(streamName)

        eventsFromStream.forEach {
            val event = mapper.readValue(it.eventData, BedEvent::class.java)
            println(mapper.writeValueAsString(event))
        }
    }

    private fun getEventType(event: BedEvent) =
        event::class.java.getAnnotation(JsonTypeName::class.java)?.value
}