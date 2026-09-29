package org.starbornag.api.application.bed

import org.starbornag.api.domain.bed.BedEvent

/** Port: announces events after they are stored, for example to the bed view over SSE. */
fun interface BedEventPublisher {
    suspend fun publish(events: List<BedEvent>)
}
