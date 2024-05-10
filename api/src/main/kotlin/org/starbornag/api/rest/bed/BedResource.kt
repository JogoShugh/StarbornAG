package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.marlonlom.utilities.timeago.TimeAgo
import org.springframework.hateoas.RepresentationModel
import org.starbornag.api.domain.bed.BedCommand
import org.starbornag.api.domain.bed.BedCommand.FertilizeBedCommand
import org.starbornag.api.domain.bed.BedCommand.PlantSeedlingInBedCommand
import org.starbornag.api.domain.bed.BedCommand.WaterBedCommand
import java.time.Instant
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = BedWatered::class, name = "watered"),
    JsonSubTypes.Type(value = BedFertilized::class, name = "fertilized"),
    JsonSubTypes.Type(value = BedMulched::class, name = "mulched")
)
open class BedEvent(
    val started : Date,
    val ended: Date = Date.from(Instant.now())
) {
    val startedDescription : String
        get() = TimeAgo.using(started.time)

    val endDescription : String
        get() = TimeAgo.using(ended.time)
}

class BedWatered(started: Date, val volume: Double) : BedEvent(started = started)
class BedFertilized(started: Date, volume: Double, val fertilizer: String) : BedEvent(started = started)
class BedMulched(started: Date, volume: Double, val material: String) : BedEvent(started = started)

data class Row(
    val cells: MutableList<String>
)

class BedAggregate(
    val id: UUID,
    val name: String,
    val rows: List<Row>
) {
    companion object {
        fun of(name: String, cellsPerRowCount: Int, rowCount: Int): BedAggregate {
            val start = 1
            val rows =
                start.rangeTo(rowCount).map {
                    Row(start.rangeTo(cellsPerRowCount).map { "" }.toMutableList())
                }

            return BedAggregate(UUID.randomUUID(), name, rows)
        }
    }

    val events = mutableListOf<BedEvent>()

    val waterings: List<BedWatered>
        get() = events.filterIsInstance<BedWatered>()

    val fertilizations: List<BedFertilized>
        get() = events.filterIsInstance<BedFertilized>()

    // Generic command handler dispatcher
    fun <T : BedCommand> execute(command: T) {
        when (command) {
            is PlantSeedlingInBedCommand -> execute(command)
            is WaterBedCommand -> execute(command)
            is FertilizeBedCommand -> execute(command)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    // Concrete command handlers
    private fun execute(command: PlantSeedlingInBedCommand) {
        // Adjust to be 1 based:
        val rowPosition = command.rowPosition - 1
        val cellPositionInRow = command.cellPositionInRow - 1
        val cells = rows[rowPosition].cells
        cells[cellPositionInRow] = "${command.plantType} - ${command.plantCultivar}"
    }

    private fun execute(command: WaterBedCommand) {
        val wateredEvent = BedWatered(command.started, command.volume)
        events.add(wateredEvent)
    }

    private fun execute(command: FertilizeBedCommand) {
        val fertilizedEvent = BedFertilized(command.started, command.volume, command.fertilizer)
        events.add(fertilizedEvent)
    }
}

class BedResource(val id: UUID,
                  val name: String,
                  val rows: List<Row>,
                  val waterings: List<BedWatered>,
                  val fertilizations: List<BedFertilized>
    ) : RepresentationModel<BedResource>() {
                      companion object {
                          fun from(bed: BedAggregate) = BedResource(
                              bed.id,
                              bed.name,
                              bed.rows,
                              bed.waterings,
                              bed.fertilizations
                          )
                      }
                  }


object BedsRepository {
    private val beds = mutableMapOf<UUID, BedAggregate>()
    fun getBed(bedId: UUID) = beds[bedId]
    fun addBed(bed: BedAggregate) {
        beds[bed.id] = bed
    }
}