package org.starbornag.api.domain.bed

import com.eventstore.dbclient.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.util.*


interface IEventStore {
    fun fetch(id: UUID, streamName: String) : Sequence<BedEvent>
    fun append(id: UUID, streamName: String, events: Iterable<BedEvent>, expectedVersion: Long)
}

@Component
class EventStoreRepository : IEventStore {
    val mapper = jacksonObjectMapper()

    private final val settings : EventStoreDBClientSettings
    // within docker: esdb-node:2113?
            = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false")
    private val client : EventStoreDBClient
            = EventStoreDBClient.create(settings)

    override fun fetch(id: UUID, streamName: String): Sequence<BedEvent> {
        val readStreamOptions = ReadStreamOptions.get()
            .fromStart()
            .notResolveLinkTos()
        val readResult = client.readStream(streamName, readStreamOptions).get()
        return sequence {
            readResult.events.forEach {
                yield(mapper.readValue(it.originalEvent.eventData, BedEvent::class.java))
            }
        }
    }

    override fun append(id: UUID, streamName: String, events: Iterable<BedEvent>, expectedVersion: Long) {
        events.forEach {
            val esEvent = EventData.builderAsJson(id,"connected", it).build()
            //val result = esClient.append("connections", esEvent)


        }
        //client.appendToStream(streamName, it).get()

        TODO("Not yet implemented")
    }
}

object BedCellRepository {
    private val bedCells = mutableMapOf<UUID, BedCellAggregate>()
    fun getBedCell(bedCellId: UUID) = bedCells.getOrElse(bedCellId) {
        throw RuntimeException("Could not find cell for id $bedCellId")
    }
    fun putBedCell(bedCell: BedCellAggregate) {
        bedCells[bedCell.id] = bedCell
    }
}