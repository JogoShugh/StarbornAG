package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.contains
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

    @Given("a client has planted {string} at {string} in the bed {string}")
    fun aClientHasPlanted(plantType: String, location: String, bed: String) {
        if (plantType == "none") return
        sendCommand("plant", location, world.bedId(bed), plantType)
        assertThat(response.statusCode()).isEqualTo(200)
    }

    @When("a client reads the bed named {string}")
    fun aClientReadsTheBedNamed(bed: String) = aClientReadsTheBed(world.bedId(bed).toString())

    @Given("a client has read the bed named {string}")
    fun aClientHasReadTheBedNamed(bed: String) {
        aClientReadsTheBedNamed(bed)
        assertThat(response.statusCode()).isEqualTo(200)
    }

    @Then("the response declares the profile {string}")
    fun theResponseDeclaresTheProfile(profile: String) {
        assertThat(response.headers().allValues("Link")).contains("<$profile>; rel=\"profile\"")
    }

    @Then("the response forms are {string}")
    fun theResponseFormsAre(forms: String) {
        val offered = body["_forms"]?.fieldNames()?.asSequence()?.toList().orEmpty()
        assertThat(offered.sorted()).isEqualTo(forms.split(" ").sorted())
    }

    @Then("the form {string} offers the plant types {string}")
    fun theFormOffersThePlantTypes(form: String, plantTypes: String) {
        val offered = body["_forms"]?.get(form)?.get("schema")?.get("properties")?.get("plantType")?.get("enum")
        assertThat(offered?.map { it.asText() }.orEmpty()).isEqualTo(plantTypes.split(" ").filter { it.isNotEmpty() })
    }

    @Then("the form {string} posts {string} to the bed's {string} link")
    fun theFormPostsTo(form: String, contentType: String, link: String) {
        val theForm = body["_forms"][form]
        assertThat(theForm["method"].asText()).isEqualTo("POST")
        assertThat(theForm["contentType"].asText()).isEqualTo(contentType)
        assertThat(theForm["_links"]["target"]["href"].asText()).isEqualTo(body["_links"][link]["href"].asText())
    }

    @Then("the form {string} requires {string}")
    fun theFormRequires(form: String, required: String) {
        val requiredFields = body["_forms"][form]["schema"]["required"].map { it.asText() }
        assertThat(requiredFields.sorted()).isEqualTo(required.split(" ").sorted())
    }

    @When("a client reads the focus {string} of the bed {string}")
    fun aClientReadsTheFocus(focus: String, bed: String) {
        send(HttpRequest.newBuilder(uri("/api/beds/${world.bedId(bed)}/focus/$focus")).GET())
    }

    @Given("a client has read the focus {string} of the bed {string}")
    fun aClientHasReadTheFocus(focus: String, bed: String) {
        aClientReadsTheFocus(focus, bed)
        assertThat(response.statusCode()).isEqualTo(200)
    }

    @Then("the focus is labelled {string}")
    fun theFocusIsLabelled(label: String) {
        assertThat(body["focus"].asText()).isEqualTo(label)
    }

    @Then("the move links are {string}")
    fun theMoveLinksAre(moves: String) {
        val links = body["_links"].fieldNames().asSequence().toList() - setOf("self", "bed", "journal")
        assertThat(links.sorted()).isEqualTo(moves.split(" ").filter { it.isNotEmpty() }.sorted())
    }

    @When("the client follows the {string} link")
    fun theClientFollowsTheLink(rel: String) {
        send(HttpRequest.newBuilder(uri(body["_links"][rel]["href"].asText())).GET())
        assertThat(response.statusCode()).isEqualTo(200)
    }

    /** The location comes from the form itself: a focus fixes it in the schema. */
    @When("the client fills the form {string} and submits it")
    fun theClientFillsTheFormAndSubmitsIt(form: String) = fillAndSubmit(form, location = null)

    /** Uses nothing but the form: fills each required field from its schema, then submits to the target. */
    @When("the client fills the form {string} with location {string} and submits it")
    fun theClientFillsTheFormWithLocationAndSubmitsIt(form: String, location: String) = fillAndSubmit(form, location)

    private fun fillAndSubmit(form: String, location: String?) {
        val theForm = body["_forms"][form]
        val properties = theForm["schema"]["properties"]
        val formLocation = properties["location"]?.get("default")?.asText()
        val payload = theForm["schema"]["required"].associate { field ->
            field.asText() to properties[field.asText()].exampleValue()
        } + mapOf("location" to (location ?: formLocation))
        val requestBody = HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))
        send(
            HttpRequest.newBuilder(uri(theForm["_links"]["target"]["href"].asText()))
                .header("Content-Type", theForm["contentType"].asText())
                .method(theForm["method"].asText(), requestBody)
        )
    }

    private fun JsonNode.exampleValue(): Any =
        when {
            has("const") -> this["const"].asText()
            has("enum") -> this["enum"][0].asText()
            this["format"]?.asText() == "date-time" -> Instant.now().toString()
            this["type"]?.asText() in setOf("number", "integer") -> 1
            else -> "example"
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
        // A bed answers with all its rows; a focus answers with its own cells, each named like "B2".
        val recorded = if (body.has("rows")) {
            body["rows"].flatMapIndexed { r, row ->
                row["cells"].mapIndexedNotNull { c, cell -> "${r + 1}:${c + 1}".takeIf { cell.hasRecorded(action) } }
            }
        } else {
            body["cells"].filter { it.hasRecorded(action) }.map { cell ->
                val position = cell["position"].asText()
                "${position[0] - 'A' + 1}:${position.drop(1)}"
            }
        }
        assertThat(recorded.joinToString(" ")).isEqualTo(cells)
    }

    @Then("the response is {string}")
    fun theResponseIs(mediaType: String) {
        assertThat(response.headers().firstValue("Content-Type").orElse("").substringBefore(";")).isEqualTo(mediaType)
    }

    @Then("the response's most recent event is {string} at {string}")
    fun theResponsesMostRecentEventIs(type: String, cells: String) {
        val latest = body["_embedded"]["recent"][0]
        assertThat(latest["type"].asText()).isEqualTo(type)
        assertThat(latest["cells"].joinToString(" ") { it.asText() }).isEqualTo(cells)
    }

    @When("an agent posts {string} as JSON to the focus {string} of the bed {string}")
    fun anAgentPostsAsJsonToTheFocus(action: String, focus: String, bed: String) {
        val payload = mapOf("plantType" to "tomato", "plantCultivar" to "Dark Galaxy", "volume" to 1.0)
        send(
            HttpRequest.newBuilder(uri("/beds/${world.bedId(bed)}/focus/$focus/$action"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/hal+json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
        )
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

    private fun sendCommand(action: String, location: String, bedId: UUID, plantType: String = "tomato") {
        val common = mapOf("bedId" to bedId, "started" to Instant.now().toString(), "location" to location)
        val details = when (action) {
            "plant" -> mapOf("plantType" to plantType, "plantCultivar" to "Dark Galaxy")
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
