package org.starbornag.api.domain.bed

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
                start.rangeTo(dimensions.rows).map { row ->
                    Row(start.rangeTo(dimensions.columns / cellBlockSize).map { column ->
                        val bc = BedCellAggregate(id, dimensions, CellPosition(row, column))
                        BedCellRepository.putBedCell(bc)
                        bc.id
                    })
                }

            return BedAggregate(id, name, rows)
        }
    }

    // TODO plant seedling command is going NOWHERE. Why?

    // Generic command handler dispatcher
    suspend fun <T : BedCommand> execute(command: T, bedEventBus: IBedEventBus) {
        when (command) {
            is CellCommand.PlantSeedling -> execute(command, bedEventBus)
            is CellCommand -> {
                val rowCount = this.rows.count()
                val columnCount = this.rows[0].cells.count()
                when {
                    command.location != null -> dispatchToStreamingCells(command, bedEventBus, command.location!!.streamCellPositions(rowCount, columnCount))
                    else -> dispatchCommandToAllCells(command, bedEventBus)
                }
            }
            else -> TODO()
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
    private suspend fun execute(command: CellCommand.PlantSeedling, bedEventBus: IBedEventBus) {
        // Adjust to be 1 based:
        val rowCount = this.rows.count()
        val columnCount = this.rows[0].cells.count()
        dispatchToStreamingCells(
            command, bedEventBus,
            // TODO deal with null:
            command.location!!.streamCellPositions(rowCount, columnCount)
        )
    }

    private suspend fun dispatchToStreamingCells(command: BedCommand, bedEventBus: IBedEventBus, cellPositions: Sequence<CellPosition>) {
        cellPositions.forEach {
            val (row, column) = it
            val cellId = this.rows[row-1].cells[column - 1]
            val cellAg = BedCellRepository.getBedCell(cellId)
            cellAg.execute(command, bedEventBus)
        }
    }
    private suspend fun dispatchCommand(command: CellCommand, bedEventBus: IBedEventBus) {
        val cells = command.location

//        val row = cells.row
//        val column = cells.column
//        val cell = cells.cell
//        when {
//            cells.isSingleCell -> dispatchToSingleCell(row!!, column!!, command, sseEventBus)
//            cells.isSingleRow-> dispatchToSingleRow(row!!, command, sseEventBus)
//            cells.isSingleColumn-> dispatchToSingleColumn(column!!, command, sseEventBus)
//            //command.isForRowWithCellSpan -> dispatchToRowWithCellSpan(row!!, cellSpan!!, command, sseEventBus)
//            else -> dispatchCommandToAllCells(command, sseEventBus)
//        }
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
        bedEventBus: IBedEventBus
    ) {
        val cellIndex = cell - 1
        coroutineScope {
            rows.forEach {
                launch {
                    val cellId = it.cells[cellIndex]
                    val cellAg = BedCellRepository.getBedCell(cellId)
                    cellAg.execute(command, bedEventBus)
                }
            }
        }
    }

    private suspend fun dispatchToSingleRow(
        row: Int,
        command: BedCommand,
        bedEventBus: IBedEventBus
    ) {
        val rowIndex = row - 1
        coroutineScope {
            rows[rowIndex].cells.forEach {
                launch {
                    val cellAg = BedCellRepository.getBedCell(it)
                    cellAg.execute(command, bedEventBus)
                }
            }
        }
    }

    private suspend fun dispatchToSingleCell(
        row: Int,
        cell: Int,
        command: BedCommand,
        bedEventBus: IBedEventBus
    ) {
        val rowIndex = row - 1
        val cellIndex = cell - 1
        val rowItem = rows[rowIndex]
        val cellIem = rowItem.cells[cellIndex]
        coroutineScope {
            launch {
                val cellAg = BedCellRepository.getBedCell(cellIem)
                cellAg.execute(command, bedEventBus)
            }
        }
    }

    private suspend fun dispatchCommandToAllCells(command: BedCommand, bedEventBus: IBedEventBus) {
        coroutineScope {
            rows.forEach { row ->
                row.cells.forEach { cellId ->
                    launch {
                        val cell = BedCellRepository.getBedCell(cellId)
                        cell.execute(command, bedEventBus)
                    }
                }
            }
        }
    }
}