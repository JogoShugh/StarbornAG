package org.starbornag.api.domain.bed

import com.eventstore.dbclient.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

@Component
class EventStoreDBRepository : IEventStoreRepository<EventData, RecordedEvent> {
    val mapper = jacksonObjectMapper()

    private final val settings : EventStoreDBClientSettings
    // within docker: esdb-node:2113?
            = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false")
    private val client : EventStoreDBClient
            = EventStoreDBClient.create(settings)

    override fun fetch(streamName: String): Sequence<RecordedEvent> {
        val readStreamOptions = ReadStreamOptions.get()
            .fromStart()
            .notResolveLinkTos()
        val readResult = client.readStream(streamName, readStreamOptions).get()
        return sequence {
            readResult.events.forEach {
                yield(it.originalEvent)
            }
        }
    }

    override fun append(streamName: String, events: Iterable<EventData>, expectedVersion: Long) {
        client.appendToStream(streamName, events.iterator()).get()

        //        events.forEach {
//            val eventType = it.eventType
//            val esEvent = EventData.builderAsJson(id,eventType,it).build()
//            val result = client.appendToStream(streamName, esEvent).get()
//            client.appendToStream()
//        }
        //client.appendToStream(streamName, it).get()
    }
}