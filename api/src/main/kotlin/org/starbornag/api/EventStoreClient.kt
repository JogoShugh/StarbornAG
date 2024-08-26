package org.starbornag.api

import com.eventstore.dbclient.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component

@Component
class EventStoreClient() {
    private val mapper = jacksonObjectMapper()

    private final val settings : EventStoreDBClientSettings
        // within docker: esdb-node:2113?
        = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false")
    private val client : EventStoreDBClient
        = EventStoreDBClient.create(settings)

    fun append(streamName: String, eventData: EventData): WriteResult? =
        client.appendToStream(streamName, eventData).get()

    fun <T> getLatestStreamEvent(streamName: String, clazz: Class<T>) : T {
        val readStreamOptions = ReadStreamOptions.get()
            .fromEnd()
            .backwards()
            .notResolveLinkTos()
        val readResult = client.readStream(streamName, readStreamOptions).get()
        val resolvedEvent = readResult.events[0]
        val writtenEventData = resolvedEvent
            .originalEvent.eventData
        val writtenEvent = mapper.readValue(writtenEventData, clazz)
        return  writtenEvent
    }
}