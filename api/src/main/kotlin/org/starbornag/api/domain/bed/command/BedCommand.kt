package org.starbornag.api.domain.bed.command

import java.time.Instant
import java.util.*

data class BedAction(
    val action: String
)

sealed class BedCommand : BedId {
    data class      PrepareBed(
        override val bedId: UUID,
        val name: String,
        val dimensions: Dimensions,
        val cellBlockSize: Int = 1
    ) : BedCommand()

    sealed class CellCommand() : BedCommand(), BedId {
        abstract val location: CellsSelection?
        abstract val action: String

        // {"bedId":"2fbda883-d49d-4067-8e16-2b04cc523111","action":"plant","started":"2023-10-10T00:00:00Z","plantType":"tomato","plantCultivar":"roma","location":{"rows":[1]}}
        data class PlantSeedling(
            override val bedId: UUID,
            override val action : String = "plant",
            val started: Date = Date.from(Instant.now()),
            val plantType: String,
            val plantCultivar: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Fertilize(
            override val bedId: UUID,
            override val action : String = "fertilize",
            val started: Date,
            val volume: Double,
            val fertilizer: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Mulch(
            override val bedId: UUID,
            override val action : String = "mulch",
            val started: Date,
            val volume: Double,
            val material: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Water(
            override val bedId: UUID,
            override val action : String = "water",
            val started: Date,
            val volume: Double = 1.0,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Harvest(
            override val bedId: UUID,
            override val action : String = "harvest",
            val started: Date,
            val plantType: String,
            val plantCultivar: String,
            val quantity: Int? = null,
            val weight: Double? = null,
            override val location: CellsSelection? = null
        ) : CellCommand()
    }
}