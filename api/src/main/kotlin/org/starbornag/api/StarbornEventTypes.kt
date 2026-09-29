package org.starbornag.api

import org.starbornag.api.domain.bed.Bed
import org.starbornag.api.domain.bed.BedCellAggregate
import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedCellWatered
import org.starbornag.api.domain.bed.BedFertilized
import org.starbornag.api.domain.bed.BedHarvested
import org.starbornag.api.domain.bed.BedMulched
import org.starbornag.api.domain.bed.Cell
import org.starbornag.eventstore.EventTypeMapper

/**
 * Stable names for stored events and stream types, so renaming or moving a Kotlin class
 * does not orphan old events. Cell event names match their @JsonTypeName values.
 */
object StarbornEventTypes {
    fun mapper(): EventTypeMapper = EventTypeMapper()
        .register(Bed::class, "bed")
        .register(Bed.Event.BedPrepared::class, "bedPrepared")
        .register(BedCellAggregate::class, "bedCell")
        .register(Cell::class, "cell")
        .register(BedCellPlanted::class, "planted")
        .register(BedCellWatered::class, "watered")
        .register(BedFertilized::class, "fertilized")
        .register(BedMulched::class, "mulched")
        .register(BedHarvested::class, "bedHarvested")
}
