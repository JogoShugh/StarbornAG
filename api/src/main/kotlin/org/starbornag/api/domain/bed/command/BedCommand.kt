package org.starbornag.api.domain.bed.command

import java.util.*

sealed class BedCommand() : BedId {
    data class PrepareBedCommand(
        override val bedId: UUID,
        val name: String,
        val dimensions: Dimensions,
        val cellBlockSize: Int = 1
    ) : BedCommand()

    data class PlantSeedlingCommand(
        override val bedId: UUID,
        val rowPosition: Int,
        val cellPositionInRow: Int,
        val plantType: String,
        val plantCultivar: String
    ) : BedCommand()

    data class FertilizeCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double,
        val fertilizer: String
    ) : BedCommand()

    data class WaterCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double
    ) : BedCommand()

    data class HarvestCommand(
        override val bedId: UUID,
        val started: Date,
        val plantType: String,
        val plantCultivar: String,
        val quantity: Int? = null,
        val weight: Double? = null
    ) : BedCommand()
}