package org.starbornag.api.domain.bed

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.Dimensions
import org.starbornag.api.domain.bed.command.Row
import java.util.*

class BedAggregate(
    val id: UUID,
    val name: String,
    val rows: List<Row>
) {
    companion object {
        fun of(id: UUID, name: String, dimensions: Dimensions, cellBlockSize: Int): BedAggregate {
            val start = 1
            val rows =
                start.rangeTo(dimensions.width).map {
                    Row(start.rangeTo(dimensions.length / cellBlockSize).map {
                        val bc = BedCellAggregate(id)
                        BedCellRepository.putBedCell(bc)
                        bc.id
                    })
                }

            return BedAggregate(id, name, rows)
        }
    }

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, emitter: SseEmitter?) {
        when (command) {
            is PlantSeedlingCommand -> execute(command, emitter)
            else ->  {
                dispatchCommandToAllCells(command, emitter)
            }
        }
    }

    // Concrete command handlers
    private suspend fun execute(command: PlantSeedlingCommand, emitter: SseEmitter?) {
        // Adjust to be 1 based:
        val rowPosition = command.rowPosition - 1
        val cellPositionInRow = command.cellPositionInRow - 1
        val row = rows[rowPosition]
        val cellId = row.cells[cellPositionInRow]
        val bedCell = BedCellRepository.getBedCell(cellId)
        bedCell.execute(command, emitter)
    }

    private suspend fun dispatchCommandToAllCells(command: BedCommand, emitter: SseEmitter?) {
        coroutineScope {
            rows.forEach { row ->
                row.cells.forEach { cellId ->
                    launch {
                        val cell = BedCellRepository.getBedCell(cellId)
                        cell.execute(command, emitter)
                    }
                }
            }
        }
    }
}