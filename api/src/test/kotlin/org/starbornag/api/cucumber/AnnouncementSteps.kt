package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.starbornag.api.domain.bed.BedEvent

class AnnouncementSteps(private val world: GardenWorld) {

    @Given("a listener is following the bed {string}")
    fun aListenerIsFollowing(bed: String) {
        world.bedId(bed)
        world.announcements.clear()
    }

    /** Announcements as "<event name> <row>:<column>", in the order they were heard. */
    @Then("the listener heard {string} for the cells {string} of the bed {string}")
    fun theListenerHeard(event: String, cells: String, bed: String) = world.blocking {
        val rows = world.beds.find(world.bedId(bed))!!.rows
        val positionOf = rows.flatMapIndexed { r, row -> row.mapIndexed { c, id -> id to "${r + 1}:${c + 1}" } }.toMap()
        val heard = world.announcements.map { "${it.typeName()} ${positionOf[it.bedCellId]}" }
        assertThat(heard).isEqualTo(cells.split(" ").map { "$event $it" })
    }

    @Then("the listener heard nothing")
    fun theListenerHeardNothing() {
        assertThat(world.announcements).isEmpty()
    }

    private fun BedEvent.typeName(): String = this::class.java.getAnnotation(JsonTypeName::class.java).value
}
