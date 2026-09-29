package org.starbornag.api.application.bed

import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.eventstore.EventStore
import org.starbornag.eventstore.Repository
import java.util.*

/** Use cases for beds. Each bed is one stream in the event store, identified by the bed id. */
class Beds(eventStore: EventStore) {
    private val repository = Repository<Bed?, Bed.Event>(eventStore, Bed::class, { null }, Bed::evolve)

    /** @throws org.starbornag.api.domain.bed.BedAlreadyExists when the bed was prepared before. */
    suspend fun prepare(command: PrepareBed): Bed =
        checkNotNull(repository.handle(command.bedId, decide = Bed.prepare(command)).state)

    suspend fun find(bedId: UUID): Bed? = repository.find(bedId)?.state
}
