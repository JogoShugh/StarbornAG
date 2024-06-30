package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEventBus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.Dimensions
import org.starbornag.api.domain.bed.command.ICellPosition
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
    suspend fun <T : BedCommand> execute(command: T, sseEventBus: SseEventBus) {
        when (command) {
            is PlantSeedlingCommand -> execute(command, sseEventBus)
            else ->  dispatchCommand(command, sseEventBus)
        }
    }

    // Concrete command handlers
    private suspend fun execute(command: PlantSeedlingCommand, sseEventBus: SseEventBus) {
        // Adjust to be 1 based:
        val rowPosition = command.rowPosition - 1
        val cellPositionInRow = command.cellPositionInRow - 1
        val row = rows[rowPosition]
        val cellId = row.cells[cellPositionInRow]
        val bedCell = BedCellRepository.getBedCell(cellId)
        bedCell.execute(command, sseEventBus)
    }

    private suspend fun dispatchCommand(command: BedCommand, sseEventBus: SseEventBus) {
        when (command) {
            is ICellPosition -> {
                println("It's an ICellPosition! -> $command")
                val row = command.row
                val cell = command.cell
                // handle case of both row and cell provided
                if (row != null && cell != null) {
                    val rowIndex = row - 1
                    val cellIndex = cell - 1
                    val rowItem = rows[rowIndex]
                    val cellIem = rowItem.cells[cellIndex]
                    coroutineScope {
                        launch {
                            val cellAg = BedCellRepository.getBedCell(cellIem)
                            cellAg.execute(command, sseEventBus)
                        }
                    }
                } else if (row != null && cell == null) {
                    val rowIndex = row - 1
                    coroutineScope {
                        rows[rowIndex].cells.forEach {
                            launch {
                                val cellAg = BedCellRepository.getBedCell(it)
                                cellAg.execute(command, sseEventBus)
                            }
                        }
                    }
                } else if (row == null && cell != null) {
                    val cellIndex = cell - 1
                    coroutineScope {
                        rows.forEach {
                            val cellId = it.cells[cellIndex]
                            val cellAg = BedCellRepository.getBedCell(cellId)
                            cellAg.execute(command, sseEventBus)
                        }
                    }
                } else {
                    dispatchCommandToAllCells(command, sseEventBus)
                }
            }
            else ->  {
                dispatchCommandToAllCells(command, sseEventBus)
            }
        }
    }

    private suspend fun dispatchCommandToAllCells(command: BedCommand, sseEventBus: SseEventBus) {
        coroutineScope {
            rows.forEach { row ->
                row.cells.forEach { cellId ->
                    launch {
                        val cell = BedCellRepository.getBedCell(cellId)
                        cell.execute(command, sseEventBus)
                    }
                }
            }
        }
    }
}