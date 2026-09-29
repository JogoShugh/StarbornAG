package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.starbornag.api.domain.bed.BedAlreadyExists
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.starbornag.api.domain.bed.command.Dimensions

class BedSteps(private val world: GardenWorld) {

    private var lastPreparation: PrepareBed? = null
    private var preparationOutcome: Result<Any>? = null

    // Cucumber matches step text regardless of Given/When, so one annotation serves both.
    @Given("the bed {string} is prepared with {int} rows, {int} columns and cell block size {int}")
    fun theBedIsPrepared(name: String, rows: Int, columns: Int, blockSize: Int) = world.blocking {
        val command = PrepareBed(world.bedId(name), name, Dimensions(rows, columns), blockSize)
        world.beds.prepare(command)
        lastPreparation = command
    }

    @When("the bed {string} is prepared again")
    fun theBedIsPreparedAgain(name: String) = world.blocking {
        val command = checkNotNull(lastPreparation) { "Prepare the bed $name first" }
        preparationOutcome = runCatching { world.beds.prepare(command) }
    }

    @When("the application restarts")
    fun theApplicationRestarts() = world.restart()

    @Then("the bed {string} has {int} rows of {int} cells")
    fun theBedHasRowsOfCells(name: String, rows: Int, cellsPerRow: Int) = world.blocking {
        val bed = world.beds.find(world.bedId(name))
        assertThat(bed).isNotNull()
        assertThat(bed!!.name).isEqualTo(name)
        assertThat(bed.rows).hasSize(rows)
        assertThat(bed.rows).each { it.hasSize(cellsPerRow) }
    }

    @Then("every cell of the bed {string} has its own id")
    fun everyCellHasItsOwnId(name: String) = world.blocking {
        val cellIds = world.beds.find(world.bedId(name))!!.rows.flatten()
        assertThat(cellIds.toSet().size).isEqualTo(cellIds.size)
    }

    @Then("the preparation is rejected because the bed already exists")
    fun thePreparationIsRejected() {
        assertThat(preparationOutcome!!.exceptionOrNull()).isNotNull().isInstanceOf(BedAlreadyExists::class)
    }
}
