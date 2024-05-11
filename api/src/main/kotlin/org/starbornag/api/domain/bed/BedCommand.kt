package org.starbornag.api.domain.bed

import java.util.*

sealed class BedCommand() : BedId {
    data class PrepareBedCommand(
        override val bedId: UUID,
        val name: String,
        val dimensions: Dimensions
    ) : BedCommand()

    data class PlantSeedlingInBedCommand(
        override val bedId: UUID,
        val rowPosition: Int,
        val cellPositionInRow: Int,
        val plantType: String,
        val plantCultivar: String
    ) : BedCommand()

    data class FertilizeBedCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double,
        val fertilizer: String
    ) : BedCommand()

    data class WaterBedCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double
    ) : BedCommand()

    data class HarvestBedCommand(
        override val bedId: UUID,
        val started: Date,
        val plantType: String,
        val plantCultivar: String,
        val quantity: Int? = null,
        val weight: Double? = null
    ) : BedCommand()
}