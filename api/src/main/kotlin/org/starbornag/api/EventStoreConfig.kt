package org.starbornag.api

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.starbornag.api.domain.bed.BedCellAggregate
import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedCellStateRepository
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedMulched
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.EventTypeMapper
import org.starbornag.eventstore.PgEventStore

@Configuration
class EventStoreConfig {

    /**
     * Stable names for stored events, so renaming or moving a Kotlin class does not orphan
     * old events. They match the events' @JsonTypeName values.
     */
    @Bean
    fun eventTypeMapper(): EventTypeMapper = EventTypeMapper()
        .register(BedCellPlanted::class, "planted")
        .register(BedCellWatered::class, "watered")
        .register(BedFertilized::class, "fertilized")
        .register(BedMulched::class, "mulched")
        .register(BedHarvested::class, "bedHarvested")
        .register(BedCellAggregate::class, "bedCell")

    /** Uses Spring Boot's pooled R2DBC connection factory (spring.r2dbc.*) and creates the schema at startup. */
    @Bean
    fun eventStore(connectionFactory: ConnectionFactory, eventTypeMapper: EventTypeMapper): EventStore =
        PgEventStore(connectionFactory, typeMapper = eventTypeMapper).also { runBlocking { it.init() } }

    @Bean
    fun bedCellStateRepository(eventStore: EventStore) = BedCellStateRepository(eventStore)
}
