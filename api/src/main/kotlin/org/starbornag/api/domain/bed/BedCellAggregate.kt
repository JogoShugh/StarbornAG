package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEvent
import ch.rasc.sse.eventbus.SseEventBus
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.coroutines.delay
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

data class Planting(
    val plantType: String,
    val plantCultivar: String
) {
    @JsonIgnore
    fun isEmpty() = plantType == ""

    override fun toString() =
        if (isEmpty()) "" else "$plantType - $plantCultivar"
}

class BedCellAggregate(
    val parentBedId: UUID,
    val id: UUID = UUID.randomUUID(),
    var planting: Planting = Planting("", "")
) {
    val events = mutableListOf<BedEvent>()

    val waterings: List<BedWatered>
        get() = events.filterIsInstance<BedWatered>()

    val fertilizations: List<BedFertilized>
        get() = events.filterIsInstance<BedFertilized>()

    val harvests: List<BedHarvested>
        get() = events.filterIsInstance<BedHarvested>()

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, sseEventBus: SseEventBus) {
        // Simulate latency
        delay(10.milliseconds)
        when (command) {
            is PlantSeedlingCommand -> execute(command)
            is WaterCommand -> execute(command, sseEventBus)
            is FertilizeCommand -> execute(command)
            is HarvestCommand -> execute(command)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: PlantSeedlingCommand) {
        planting = Planting(command.plantType, command.plantCultivar)
    }

    private fun execute(command: WaterCommand, sseEventBus: SseEventBus) {
        val wateredEvent = BedWatered(command.started, command.volume)
        events.add(wateredEvent)
        sseEventBus.handleEvent(SseEvent.of(command.bedId.toString(), wateredEvent))
    }

    private fun execute(command: FertilizeCommand) {
        val fertilizedEvent = BedFertilized(command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
    }

    private fun execute(command: HarvestCommand) {
        val harvestedEvent = BedHarvested(
            command.started,
            command.plantType,
            command.plantCultivar,
            command.quantity,
            command.weight
        )
        events.add(harvestedEvent)
    }
}
