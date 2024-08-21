package org.starbornag.api.domain.bed.command

import java.util.*

sealed class BedCommand : BedId {
    data class PrepareBed(
        override val bedId: UUID,
        val name: String,
        val dimensions: Dimensions,
        val cellBlockSize: Int = 1
    ) : BedCommand()

    sealed class CellCommand() : BedCommand(), BedId {
        abstract val location: CellsSelection?

        data class PlantSeedling(
            override val bedId: UUID,
            val started: Date,
            val plantType: String,
            val plantCultivar: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Fertilize(
            override val bedId: UUID,
            val started: Date,
            val volume: Double,
            val fertilizer: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Mulch(
            override val bedId: UUID,
            val started: Date,
            val volume: Double,
            val material: String,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Water(
            override val bedId: UUID,
            val started: Date,
            val volume: Double,
            override val location: CellsSelection? = null
        ) : CellCommand()

        data class Harvest(
            override val bedId: UUID,
            val started: Date,
            val plantType: String,
            val plantCultivar: String,
            val quantity: Int? = null,
            val weight: Double? = null,
            override val location: CellsSelection? = null
        ) : CellCommand()
    }
}