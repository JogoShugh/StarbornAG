package org.starbornag.api.domain.bed

import ch.rasc.sse.eventbus.SseEventBus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.CellPosition
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

    // TODO plant seedling command is going NOWHERE. Why?

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, sseEventBus: SseEventBus) {
        when (command) {
            is PlantSeedlingCommand -> execute(command, sseEventBus)
            else ->  dispatchCommand(command, sseEventBus)
        }
    }

//    // Concrete command handlers
//    private suspend fun execute(command: PlantSeedlingCommand, sseEventBus: SseEventBus) {
//        // Adjust to be 1 based:
//        val rowPosition = command.cells.row!! - 1
//        val cellPositionInRow = command.cells.column!! - 1
//        val row = rows[rowPosition]
//        val cellId = row.cells[cellPositionInRow]
//        val bedCell = BedCellRepository.getBedCell(cellId)
//        bedCell.execute(command, sseEventBus)
//    }

    // Concrete command handlers
    private suspend fun execute(command: PlantSeedlingCommand, sseEventBus: SseEventBus) {
        // Adjust to be 1 based:
        if (command.location != null) {
            val rowCount = this.rows.count()
            val columnCount = this.rows[0].cells.count()
            dispatchToStreamingCells(
                command, sseEventBus,
                command.location.streamCellPositions(rowCount, columnCount)
            )
        }
        else {
            TODO()
        }
    }

    private suspend fun dispatchToStreamingCells(command: BedCommand, sseEventBus: SseEventBus, cellPositions: Sequence<CellPosition>) {
        cellPositions.forEach {
            val (row, column) = it
            val cellId = this.rows[row-1].cells[column - 1]
            val cellAg = BedCellRepository.getBedCell(cellId)
            cellAg.execute(command, sseEventBus)
        }
    }
    private suspend fun dispatchCommand(command: BedCommand, sseEventBus: SseEventBus) {
        val cells = command.cells

        val row = cells!!.row
        val column = cells.column
        val cell = cells.cell
        when {
            cells.isSingleCell -> dispatchToSingleCell(row!!, column!!, command, sseEventBus)
            cells.isSingleRow-> dispatchToSingleRow(row!!, command, sseEventBus)
            cells.isSingleColumn-> dispatchToSingleColumn(column!!, command, sseEventBus)
            //command.isForRowWithCellSpan -> dispatchToRowWithCellSpan(row!!, cellSpan!!, command, sseEventBus)
            else -> dispatchCommandToAllCells(command, sseEventBus)
        }
    }


//    private suspend fun dispatchToRowWithCellSpan(row: Int, cellSpan: Int, command: BedCommand, sseEventBus: SseEventBus) {
//        val rowIndex = row - 1
//        val cells = rows[rowIndex].cells
//        coroutineScope {
//            cells.take(cellSpan).forEach {
//                launch {
//                    val cellAg = BedCellRepository.getBedCell(it)
//                    cellAg.execute(command, sseEventBus)
//                }
//            }
//        }
//    }

    private suspend fun dispatchToSingleColumn(
        cell: Int,
        command: BedCommand,
        sseEventBus: SseEventBus
    ) {
        val cellIndex = cell - 1
        coroutineScope {
            rows.forEach {
                launch {
                    val cellId = it.cells[cellIndex]
                    val cellAg = BedCellRepository.getBedCell(cellId)
                    cellAg.execute(command, sseEventBus)
                }
            }
        }
    }

    private suspend fun dispatchToSingleRow(
        row: Int,
        command: BedCommand,
        sseEventBus: SseEventBus
    ) {
        val rowIndex = row - 1
        coroutineScope {
            rows[rowIndex].cells.forEach {
                launch {
                    val cellAg = BedCellRepository.getBedCell(it)
                    cellAg.execute(command, sseEventBus)
                }
            }
        }
    }

    private suspend fun dispatchToSingleCell(
        row: Int,
        cell: Int,
        command: BedCommand,
        sseEventBus: SseEventBus
    ) {
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