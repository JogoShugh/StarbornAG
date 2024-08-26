package org.starbornag.api.domain.bed

import com.eventstore.dbclient.EventData
import net.bytebuddy.implementation.bind.annotation.IgnoreForBinding
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.starbornag.api.Connected
import org.starbornag.api.EventStoreClient
import java.util.*

class EventStoreClientTest {

    @Test
    @Disabled
    fun `connects and writes and reads simple event`() {
        val esClient = EventStoreClient()
        val connected = Connected(UUID.randomUUID(), true)
        val esEvent = EventData.builderAsJson(connected.id,"connected", connected).build()
        val result = esClient.append("connections", esEvent)
        val writtenEvent = esClient.getLatestStreamEvent("connections", Connected::class.java)

        println("Event created in memory:")
        println(connected)
        println("Resolved event from EventStoreDB connections stream:")
        println(writtenEvent)
    }
}