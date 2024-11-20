package org.starbornag.api.domain.bed

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.coroutines.delay
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.Dimensions
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

data class Harvest(
    val planting : Planting,
    val quantity: Int? = null,
    val weight: Double? = null
)
class BedCellAggregateEventSourced(
    private val parentBedId: UUID,
    private val id: UUID,
    private val state: BedCellAggregateState
) {
    companion object {
        private val cellStateRepository = BedCellStateRepository()

        fun of(parentBedId: UUID, id: UUID): BedCellAggregateEventSourced {
            val state = cellStateRepository.fetch(parentBedId, id)
            return BedCellAggregateEventSourced(parentBedId, id, state)
        }
    }

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, bedEventBus: IBedEventBus) {
        // Simulate latency
        delay(1.milliseconds)
        when (command) {
            is CellCommand.PlantSeedling -> execute(command, bedEventBus)
            is CellCommand.Water -> execute(command, bedEventBus)
            is CellCommand.Fertilize -> execute(command, bedEventBus)
            is CellCommand.Harvest -> execute(command)
            is CellCommand.Mulch -> execute(command, bedEventBus)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private suspend fun execute(command: CellCommand.PlantSeedling, bedEventBus: IBedEventBus) =
        storeAndPublish(command, bedEventBus) {
            BedCellPlanted(parentBedId, id, command.started, command.plantType, command.plantCultivar)
        }

    private suspend fun execute(command: CellCommand.Water, bedEventBus: IBedEventBus) =
        storeAndPublish(command, bedEventBus) {
            BedCellWatered(parentBedId, id, command.started, command.volume)
        }

    private suspend fun execute(command: CellCommand.Fertilize, bedEventBus: IBedEventBus) =
        storeAndPublish(command, bedEventBus) {
            BedFertilized(parentBedId, id, command.started, command.volume, command.fertilizer)
        }

    private suspend fun execute(command: CellCommand.Mulch, bedEventBus: IBedEventBus) =
        storeAndPublish(command, bedEventBus) {
            BedMulched(parentBedId, id, command.started, command.volume, command.material)
        }

    private fun execute(command: CellCommand.Harvest) {
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

    private suspend fun <T: CellCommand> storeAndPublish(command: T, bedEventBus: IBedEventBus,
                                eventCreator: (command: T) -> BedEvent) {
        val event = eventCreator(command)
        storeEvent(event)
        publishEvent(bedEventBus, command, event)
    }

    private suspend fun publishEvent(
        bedEventBus: IBedEventBus,
        command: CellCommand,
        event: BedEvent
    ) = bedEventBus.publishEvent(command, event)

    private fun storeEvent(event: BedEvent) =
        cellStateRepository.append(parentBedId, id, listOf(event))
}



class BedCellAggregate(
    val parentBedId: UUID,
    val parentBedDimension: Dimensions,
    val cellPosition: CellPosition,
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
    suspend fun <T : BedCommand> execute(command: T, bedEventBus: IBedEventBus) {
        when (command) {
            is CellCommand.PlantSeedling -> execute(command, bedEventBus)
            is CellCommand.Water -> execute(command, bedEventBus)
            is CellCommand.Fertilize -> execute(command, bedEventBus)
            is CellCommand.Harvest -> execute(command)
            is CellCommand.Mulch -> execute(command, bedEventBus)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private suspend fun execute(command: CellCommand.PlantSeedling, bedEventBus: IBedEventBus) {
        val plantedEvent = BedCellPlanted(parentBedId, id, command.started, command.plantType, command.plantCultivar)
        planting = Planting(command.plantType, command.plantCultivar)
        events.add(plantedEvent)
        bedEventBus.publishEvent(command, plantedEvent)
    }

    private suspend fun execute(command: CellCommand.Water, bedEventBus: IBedEventBus) {
        val wateredEvent = BedCellWatered(parentBedId, id, command.started, command.volume)
        events.add(wateredEvent)
        bedEventBus.publishEvent(command, wateredEvent)
    }

    private suspend fun execute(command: CellCommand.Fertilize, bedEventBus: IBedEventBus) {
        val fertilizedEvent = BedFertilized(parentBedId, id, command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
        bedEventBus.publishEvent(command, fertilizedEvent)
    }

    private suspend fun execute(command: CellCommand.Mulch, bedEventBus: IBedEventBus) {
        val mulchedEvent = BedMulched(parentBedId, id, command.started, command.volume, command.material)
        events.add(mulchedEvent)
        bedEventBus.publishEvent(command, mulchedEvent)
    }

    private fun execute(command: CellCommand.Harvest) {
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
