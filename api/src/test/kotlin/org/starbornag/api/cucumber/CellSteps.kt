package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.starbornag.api.application.bed.UnknownBed
import org.starbornag.api.domain.bed.BedCell
import org.starbornag.api.domain.bed.CellAlreadyPlanted
import org.starbornag.api.domain.bed.NothingToHarvest
import org.starbornag.api.domain.bed.LocationOutsideBed
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.CellsSelection
import java.util.*

class CellSteps(private val world: GardenWorld) {

    private var commandOutcome: Result<Any>? = null

    @When("{string} is done at {string} in the bed {string}")
    fun actionIsDoneInBed(action: String, location: String, bed: String) =
        run(command(action, world.bedId(bed), CellsSelection.fromString(location)))

    @When("{string} is done at {string} in the unknown bed")
    fun actionIsDoneInUnknownBed(action: String, location: String) =
        run(command(action, UUID.randomUUID(), CellsSelection.fromString(location)))

    @When("{string} is done everywhere in the bed {string}")
    fun actionIsDoneEverywhere(action: String, bed: String) = run(command(action, world.bedId(bed), null))

    @When("{word} {string} is planted at {string} in the bed {string}")
    fun isPlanted(plantType: String, cultivar: String, location: String, bed: String) = run(
        CellCommand.PlantSeedling(
            world.bedId(bed), plantType = plantType, plantCultivar = cultivar,
            location = CellsSelection.fromString(location)
        )
    )

    @When("{string} is watered with {double} in the bed {string}")
    fun isWatered(location: String, volume: Double, bed: String) = run(
        CellCommand.Water(
            world.bedId(bed), started = Date(), volume = volume, location = CellsSelection.fromString(location)
        )
    )

    @Then("the cells of the bed {string} with a recorded {string} are {string}")
    fun theCellsWithARecordedActionAre(bed: String, action: String, cells: String) {
        assertThat(cellsWithRecorded(bed, action).joinToString(" ") { "${it.row}:${it.column}" }).isEqualTo(cells)
    }

    @Then("all {int} cells of the bed {string} have a recorded {string}")
    fun allCellsHaveARecordedAction(count: Int, bed: String, action: String) {
        assertThat(cellsWithRecorded(bed, action).size).isEqualTo(count)
    }

    @Then("no cell of the bed {string} has a recorded {string}")
    fun noCellHasARecordedAction(bed: String, action: String) {
        assertThat(cellsWithRecorded(bed, action)).isEmpty()
    }

    @Then("the cell {string} of the bed {string} shows:")
    fun theCellShows(location: String, bed: String, table: DataTable) = world.blocking {
        val expected = table.asMaps().single()
        val position = CellPosition.of(location)
        val cellId = world.beds.find(world.bedId(bed))!!.rows[position.row - 1][position.column - 1]
        val cell = world.cells.find(cellId)!!
        assertThat(cell.plantings.joinToString(", ")).isEqualTo(expected["planting"])
        assertThat(cell.lastWatered?.volume).isEqualTo(expected["last watered volume"]!!.toDouble())
    }

    @When("{word} is harvested at {string} in the bed {string}")
    fun isHarvested(plantType: String, location: String, bed: String) = run(
        CellCommand.Harvest(
            world.bedId(bed), started = Date(), plantType = plantType, plantCultivar = "", quantity = 1,
            location = CellsSelection.fromString(location)
        )
    )

    @Then("the command is rejected because a cell is already planted")
    fun rejectedBecauseACellIsAlreadyPlanted() {
        assertThat(commandOutcome!!.exceptionOrNull()).isNotNull().isInstanceOf(CellAlreadyPlanted::class)
    }

    @Then("the command is rejected because nothing there can be harvested")
    fun rejectedBecauseNothingCanBeHarvested() {
        assertThat(commandOutcome!!.exceptionOrNull()).isNotNull().isInstanceOf(NothingToHarvest::class)
    }

    @Then("the cell {string} of the bed {string} is still growing {string}")
    fun theCellIsStillGrowing(location: String, bed: String, planting: String) = world.blocking {
        val position = CellPosition.of(location)
        val cellId = world.beds.find(world.bedId(bed))!!.rows[position.row - 1][position.column - 1]
        assertThat(world.cells.find(cellId)!!.plantings.last().toString()).isEqualTo(planting)
    }

    @Then("the command is rejected because the bed does not exist")
    fun rejectedBecauseTheBedDoesNotExist() {
        assertThat(commandOutcome!!.exceptionOrNull()).isNotNull().isInstanceOf(UnknownBed::class)
    }

    @Then("the command is rejected because the location is outside the bed")
    fun rejectedBecauseTheLocationIsOutsideTheBed() {
        assertThat(commandOutcome!!.exceptionOrNull()).isNotNull().isInstanceOf(LocationOutsideBed::class)
    }

    private fun run(command: CellCommand) = world.blocking {
        commandOutcome = runCatching { world.cells.handle(command) }
    }

    /** Positions, in row-major order, of the cells whose history includes [action]. */
    private fun cellsWithRecorded(bed: String, action: String): List<CellPosition> = world.blocking {
        val rows = world.beds.find(world.bedId(bed))!!.rows
        rows.flatMapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { columnIndex, cellId ->
                val cell = world.cells.find(cellId)
                CellPosition(rowIndex + 1, columnIndex + 1).takeIf { cell != null && cell.hasRecorded(action) }
            }
        }
    }

    private fun BedCell.hasRecorded(action: String): Boolean =
        when (action) {
            "plant" -> plantings.isNotEmpty()
            "water" -> lastWatered != null
            "fertilize" -> lastFertilized != null
            "mulch" -> lastMulched != null
            "harvest" -> harvests.isNotEmpty()
            else -> throw IllegalArgumentException("Unknown action '$action'")
        }

    private fun command(action: String, bedId: UUID, location: CellsSelection?): CellCommand {
        val now = Date()
        return when (action) {
            "plant" -> CellCommand.PlantSeedling(
                bedId, started = now, plantType = "tomato", plantCultivar = "Dark Galaxy", location = location
            )
            "water" -> CellCommand.Water(bedId, started = now, volume = 1.0, location = location)
            "fertilize" -> CellCommand.Fertilize(
                bedId, started = now, volume = 1.0, fertilizer = "Vegan Mix 3-2-2", location = location
            )
            "mulch" -> CellCommand.Mulch(bedId, started = now, volume = 1.0, material = "straw", location = location)
            "harvest" -> CellCommand.Harvest(
                bedId, started = now, plantType = "tomato", plantCultivar = "Dark Galaxy", quantity = 3,
                location = location
            )
            else -> throw IllegalArgumentException("Unknown action '$action'")
        }
    }
}
