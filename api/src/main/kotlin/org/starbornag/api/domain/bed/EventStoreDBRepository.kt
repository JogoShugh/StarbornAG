package org.starbornag.api.domain.bed

import com.eventstore.dbclient.*
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class EventStoreDBRepository<T>(private val clazz: Class<T>) : IEventStoreRepository<T> {
    val mapper = jacksonObjectMapper()

    // within docker: esdb-node:2113?
    private final val settings =
        EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false")
    private val client =
        EventStoreDBClient.create(settings)

    override fun fetch(streamName: String): Sequence<T> {
        val readStreamOptions = ReadStreamOptions.get()
            .fromStart()
            .notResolveLinkTos()
        val readResult = client.readStream(streamName, readStreamOptions).get()
        return sequence {
            readResult.events.forEach {
                val event = mapper.readValue(it.originalEvent.eventData, clazz)
                yield(event)
            }
        }
    }

    override fun append(streamName: String, events: Iterable<T>, expectedVersion: Long) {
        val eventsAsEventData = events.map {
            EventData.builderAsJson(UUID.randomUUID(), getEventType(it), it).build()
        }
        client.appendToStream(streamName, eventsAsEventData.iterator()).get()
    }

    private fun getEventType(event: T) =
        event!!::class.java.getAnnotation(JsonTypeName::class.java)?.value
}