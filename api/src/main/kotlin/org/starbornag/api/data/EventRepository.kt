package org.starbornag.api.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Table("events")
data class Event(
    @Id
    val id: UUID,
    val data: String, // Use String to represent jsonb
    val streamId: UUID,
    val type: String,
    val version: Long
)

interface EventRepository : CoroutineCrudRepository<Event, UUID> {
    @Modifying
    @Transactional
    @Query("SELECT append_event(:eventId, :data::jsonb, :eventType, :streamId, :streamType, :expectedVersion)")
    suspend fun appendEvent(eventId: UUID, data: String, eventType: String, streamId: UUID, streamType: String, expectedVersion: Long) : Boolean

    @Query("SELECT id, data, stream_id, type, version, created FROM events where stream_id = :streamId ORDER BY version")
    suspend fun getEventsByStreamId(streamId: UUID) : Flow<Event>
}