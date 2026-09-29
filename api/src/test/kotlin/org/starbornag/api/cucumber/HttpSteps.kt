package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.starbornag.api.testsupport.TestApplication
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.*

/** Drives the running application over real HTTP, like the bed page or an agent would. */
class HttpSteps(private val world: GardenWorld) {

    private val http = HttpClient.newHttpClient()
    private val json = jacksonObjectMapper()
    private lateinit var response: HttpResponse<String>

    private val body: JsonNode get() = json.readTree(response.body())

    @When("a client prepares the bed {string} with {int} rows, {int} columns and cell block size {int}")
    fun aClientPreparesTheBed(name: String, rows: Int, columns: Int, blockSize: Int) {
        val command = mapOf(
            "bedId" to world.bedId(name), "name" to name,
            "dimensions" to mapOf("rows" to rows, "columns" to columns), "cellBlockSize" to blockSize
        )
        post("/api/beds", command)
    }

    @Given("a client has prepared the bed {string} with {int} rows, {int} columns and cell block size {int}")
    fun aClientHasPreparedTheBed(name: String, rows: Int, columns: Int, blockSize: Int) {
        aClientPreparesTheBed(name, rows, columns, blockSize)
        assertThat(response.statusCode()).isEqualTo(201)
    }

    @When("a client sends {string} at {string} to the bed {string}")
    fun aClientSendsToTheBed(action: String, location: String, bed: String) =
        sendCommand(action, location, world.bedId(bed))

    @When("a client sends {string} at {string} to the unknown bed")
    fun aClientSendsToTheUnknownBed(action: String, location: String) =
        sendCommand(action, location, UUID.randomUUID())

    @When("a client reads the bed {word}")
    fun aClientReadsTheBed(bedId: String) {
        send(HttpRequest.newBuilder(uri("/api/beds/$bedId")).GET())
    }

    @Then("the response status is {int}")
    fun theResponseStatusIs(status: Int) {
        assertThat(response.statusCode()).isEqualTo(status)
    }

    @Then("the response has a Location header for the bed {string}")
    fun theResponseHasALocationHeader(bed: String) {
        assertThat(response.headers().firstValue("Location").orElse("")).isEqualTo("/api/beds/${world.bedId(bed)}")
    }

    @Then("the response links are {string}")
    fun theResponseLinksAre(rels: String) {
        val links = body["_links"].fieldNames().asSequence().toList()
        assertThat(links.sorted()).isEqualTo(rels.split(" ").sorted())
    }

    @Then("the cells with a recorded {string} in the response are {string}")
    fun theCellsWithARecordedActionAre(action: String, cells: String) {
        val recorded = body["rows"].flatMapIndexed { r, row ->
            row["cells"].mapIndexedNotNull { c, cell -> "${r + 1}:${c + 1}".takeIf { cell.hasRecorded(action) } }
        }
        assertThat(recorded.joinToString(" ")).isEqualTo(cells)
    }

    @Then("the response bed is named {string} with {int} rows of {int} cells")
    fun theResponseBedIs(name: String, rows: Int, cellsPerRow: Int) {
        assertThat(body["name"].asText()).isEqualTo(name)
        val rowSizes = body["rows"].map { it["cells"].size() }
        assertThat(rowSizes).hasSize(rows)
        assertThat(rowSizes).each { it.isEqualTo(cellsPerRow) }
    }

    private fun JsonNode.hasRecorded(action: String): Boolean =
        when (action) {
            "plant" -> this["planting"]["plantType"].asText().isNotEmpty()
            "water" -> !this["lastWatering"].isNull
            else -> throw IllegalArgumentException("Unknown action '$action'")
        }

    private fun sendCommand(action: String, location: String, bedId: UUID) {
        val common = mapOf("bedId" to bedId, "started" to Instant.now().toString(), "location" to location)
        val details = when (action) {
            "plant" -> mapOf("plantType" to "tomato", "plantCultivar" to "Dark Galaxy")
            "water" -> mapOf("volume" to 1.0)
            else -> throw IllegalArgumentException("Unknown action '$action'")
        }
        post("/api/beds/$bedId/$action", common + details)
    }

    private fun post(path: String, payload: Any) {
        send(
            HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
        )
    }

    private fun send(request: HttpRequest.Builder) {
        response = http.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    private fun uri(path: String) = URI.create(TestApplication.baseUrl + path)
}
