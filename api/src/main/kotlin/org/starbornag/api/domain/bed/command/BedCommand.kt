package org.starbornag.api.domain.bed.command

import java.util.*

sealed class BedCommand(row: Int? = null, column: Int? = null) : BedId {
    val cells: CellsSelection? = null
    data class PrepareBedCommand(
        override val bedId: UUID,
        val name: String,
        val dimensions: Dimensions,
        val cellBlockSize: Int = 1
    ) : BedCommand()

    data class PlantSeedlingCommand(
        override val bedId: UUID,
        val started: Date,
//        val row: Int,
//        val column: Int,
        val plantType: String,
        val plantCultivar: String,
        val location: CellsSelection
    ) : BedCommand()

    data class FertilizeCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double,
        val fertilizer: String
    ) : BedCommand()

    data class MulchCommand(
        override val bedId: UUID,
        val started: Date,
        val volume: Double,
        val material: String
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