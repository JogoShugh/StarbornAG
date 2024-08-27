package org.starbornag.api.domain.bed

import java.util.*

object BedCellRepository {
    private val bedCells = mutableMapOf<UUID, BedCellAggregate>()
    fun getBedCell(bedCellId: UUID) = bedCells.getOrElse(bedCellId) {
        throw RuntimeException("Could not find cell for id $bedCellId")
    }
    fun putBedCell(bedCell: BedCellAggregate) {
        bedCells[bedCell.id] = bedCell
    }
}