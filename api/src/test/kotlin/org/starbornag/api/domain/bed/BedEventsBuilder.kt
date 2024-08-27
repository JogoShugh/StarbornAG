package org.starbornag.api.domain.bed

import java.util.*

fun buildBedEvents(bedId: UUID, cellId: UUID, block: BedEventsBuilder.() -> Unit) =
    BedEventsBuilder(bedId, cellId).apply(block).build()

class BedEventsBuilder(private val bedId: UUID, private val cellId: UUID) {
    private val events = mutableListOf<BedEvent>()

    fun planted(date: Date, plantType: String, cultivar: String) =
        events.add(BedCellPlanted(bedId, cellId, date, plantType, cultivar))

    fun watered(date: Date, volume: Double) =
        events.add(BedCellWatered(bedId, cellId, date, volume))

    fun fertilized(date: Date, volume: Double, fertilizer: String) =
        events.add(BedFertilized(bedId, cellId, date, volume, fertilizer))

    fun build() = events.asSequence()
}