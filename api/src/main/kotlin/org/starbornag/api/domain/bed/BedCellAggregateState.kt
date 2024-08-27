package org.starbornag.api.domain.bed

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.*
import java.util.*

data class BedCellAggregateState(
    val plantings: List<Planting>? = null,
    val watered: BedCellWatered? = null,
    val fertilized: BedFertilized? = null,
    val mulched: BedMulched? = null,
    val harvests: List<Harvest>? = null
    ) {

    companion object {
        private const val SEASON_MONTHS_DURATION : Long = 8

        private fun withinPastWeek(date: Date) = date.after(Date.from(
            ZonedDateTime.now().minus(1, WEEKS ).toInstant()
        ))

        private fun withinPastMonths(date: Date, numberOfMonths: Long) = date.after(Date.from(
            ZonedDateTime.now().minus(numberOfMonths, MONTHS ).toInstant()
        ))

        private fun withinPastWeeks(date: Date, numberOfWeeks: Long) = date.after(Date.from(
            ZonedDateTime.now().minus(numberOfWeeks, WEEKS ).toInstant()
        ))

        fun fromEvents(events: Sequence<BedEvent>) =
            events.fold(BedCellAggregateState()) { state, event ->
                when (event) {
                    is BedCellPlanted -> apply(state, event)
                    is BedCellWatered -> apply(state, event)
                    is BedFertilized -> apply(state, event)
                    is BedHarvested -> apply(state, event)
                    is BedMulched -> apply(state, event)
                }
            }

        private fun apply(state: BedCellAggregateState, event: BedCellPlanted) =
            if (withinPastMonths(event.started, SEASON_MONTHS_DURATION)) {
                val newPlanting = Planting(event.plantType, event.plantCultivar)
                val updatedPlantings =
                    if (state.plantings != null)
                        listOf(*state.plantings.toTypedArray(), newPlanting)
                    else listOf(newPlanting)
                state.copy(
                    plantings = updatedPlantings
                )
            } else state

        private fun apply(state: BedCellAggregateState, event: BedCellWatered) =
            if (withinPastWeek(event.started)) {
                state.copy(watered = event)
            } else state

        private fun apply(state: BedCellAggregateState, event: BedFertilized) =
            if (withinPastMonths(event.started, 1)) {
                state.copy(fertilized = event)
            } else state

        private fun apply(state: BedCellAggregateState, event: BedMulched) =
            if (withinPastMonths(event.started, SEASON_MONTHS_DURATION)) {
                state.copy(mulched = event)
            } else state

        private fun apply(state: BedCellAggregateState, event: BedHarvested) =
            if (withinPastWeeks(event.started, 2)) {
                val newHarvest = Harvest(
                    Planting(event.plantType, event.plantCultivar),
                    event.quantity,
                    event.weight
                )
                val updatedHarvests =
                    if (state.harvests != null)
                        listOf(*state.harvests.toTypedArray(), newHarvest)
                    else listOf(newHarvest)
                state.copy(
                    harvests = updatedHarvests
                )
            } else state
    }
}