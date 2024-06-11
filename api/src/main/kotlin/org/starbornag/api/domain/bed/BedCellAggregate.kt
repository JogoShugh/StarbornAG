package org.starbornag.api.domain.bed

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.rest.bed.BedCommandHandler
import org.starbornag.api.rest.bed.BedCommandHandler.Companion.emitters
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
    suspend fun <T : BedCommand> execute(command: T, emitter: SseEmitter?) {
        // Simulate latency
        delay(10.milliseconds)
        when (command) {
            is PlantSeedlingCommand -> execute(command)
            is WaterCommand -> execute(command, emitter)
            is FertilizeCommand -> execute(command)
            is HarvestCommand -> execute(command)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: PlantSeedlingCommand) {
        planting = Planting(command.plantType, command.plantCultivar)
    }

    private fun execute(command: WaterCommand, emitter: SseEmitter?) {
        val wateredEvent = BedWatered(command.started, command.volume)
        events.add(wateredEvent)
        val event = SseEmitter.event()
            .id(id.toString())
            .name("BedCellWatered")
            .data(wateredEvent) // Send the cell object itself
        try {
            emitter?.send(event)
        } catch (e: Exception) {
            println("The error: $e")
        }
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
