package org.starbornag.api

import ch.rasc.sse.eventbus.SseEventBus
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.starbornag.api.application.bed.BedEventPublisher
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.application.bed.Cells
import org.starbornag.api.sse.SseBedEventPublisher
import org.starbornag.api.domain.bed.BedCellStateRepository
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.EventTypeMapper
import org.starbornag.eventstore.PgEventStore

@Configuration
class EventStoreConfig {

    @Bean
    fun eventTypeMapper(): EventTypeMapper = StarbornEventTypes.mapper()

    /** Uses Spring Boot's pooled R2DBC connection factory (spring.r2dbc.*) and creates the schema at startup. */
    @Bean
    fun eventStore(connectionFactory: ConnectionFactory, eventTypeMapper: EventTypeMapper): EventStore =
        PgEventStore(connectionFactory, typeMapper = eventTypeMapper).also { runBlocking { it.init() } }

    @Bean
    fun bedCellStateRepository(eventStore: EventStore) = BedCellStateRepository(eventStore)

    @Bean
    fun beds(eventStore: EventStore) = Beds(eventStore)

    @Bean
    fun bedEventPublisher(sseEventBus: SseEventBus): BedEventPublisher = SseBedEventPublisher(sseEventBus)

    @Bean
    fun cells(eventStore: EventStore, beds: Beds, publisher: BedEventPublisher) = Cells(eventStore, beds, publisher)
}
