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

    val waterings: List<BedCellWatered>
        get() = events.filterIsInstance<BedCellWatered>()

    val fertilizations: List<BedFertilized>
        get() = events.filterIsInstance<BedFertilized>()

    val harvests: List<BedHarvested>
        get() = events.filterIsInstance<BedHarvested>()

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, sseEventBus: SseEventBus) {
        // Simulate latency
        delay(10.milliseconds)
        when (command) {
            is PlantSeedlingCommand -> execute(command, sseEventBus)
            is WaterCommand -> execute(command, sseEventBus)
            is FertilizeCommand -> execute(command, sseEventBus)
            is HarvestCommand -> execute(command)
            is MulchCommand -> execute(command, sseEventBus)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: PlantSeedlingCommand, sseEventBus: SseEventBus) {
        val plantedEvent = BedCellPlanted(parentBedId, id, command.started, command.plantType, command.plantCultivar)
        planting = Planting(command.plantType, command.plantCultivar)
        events.add(plantedEvent)
        sseEventBus.handleEvent(SseEvent.of(command.bedId.toString(), plantedEvent))
    }

    private fun execute(command: WaterCommand, sseEventBus: SseEventBus) {
        val wateredEvent = BedCellWatered(parentBedId, id, command.started, command.volume)
        events.add(wateredEvent)
        sseEventBus.handleEvent(SseEvent.of(command.bedId.toString(), wateredEvent))
    }

    private fun execute(command: FertilizeCommand, sseEventBus: SseEventBus) {
        val fertilizedEvent = BedFertilized(parentBedId, id, command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
        sseEventBus.handleEvent(SseEvent.of(command.bedId.toString(), fertilizedEvent))
    }

    private fun execute(command: MulchCommand, sseEventBus: SseEventBus) {
        val mulchedEvent = BedMulched(parentBedId, id, command.started, command.volume, command.material)
        events.add(mulchedEvent)
        sseEventBus.handleEvent(SseEvent.of(command.bedId.toString(), mulchedEvent))
    }

    private fun execute(command: HarvestCommand) {
//        val harvestedEvent = BedHarvested(
//            parentBedId,
//            command.started,
//            command.plantType,
//            command.plantCultivar,
//            command.quantity,
//            command.weight
//        )
//        events.add(harvestedEvent)
    }
}
