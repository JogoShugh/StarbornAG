package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.jsoup.Jsoup
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Asks the same addresses as a browser and as an agent, and compares what each is offered.
 */
class NegotiationSteps(private val world: GardenWorld, private val page: PageSteps) {

    private lateinit var answer: HttpResponse<String>
    private val json: JsonNode get() = jacksonObjectMapper().readTree(answer.body())

    @When("a client asks for {string} of the bed {string} accepting {string}")
    fun aClientAsksFor(address: String, bed: String, accept: String) {
        val rest = address.removePrefix("/").let { if (it.isEmpty()) "" else "/$it" }
        answer = ask("/beds/${world.bedId(bed)}$rest", accept)
        assertThat(answer.statusCode()).isEqualTo(200)
    }

    @When("a client asks for {string} accepting {string}")
    fun aClientAsksForTheAddress(address: String, accept: String) {
        answer = ask(address, accept)
        assertThat(answer.statusCode()).isEqualTo(200)
    }

    @When("the client follows the answer's {string} link")
    fun theClientFollowsTheAnswersLink(rel: String) =
        aClientAsksForTheAddress(json["_links"][rel]["href"].asText(), "application/hal+json")

    @Then("the answer links {string} to {string}")
    fun theAnswerLinksTo(rel: String, href: String) {
        assertThat(json["_links"][rel]["href"].asText()).isEqualTo(href)
    }

    @Then("the listed beds include:")
    fun theListedBedsInclude(table: DataTable) {
        val listed = json["_embedded"]["beds"].map { bed ->
            mapOf("name" to bed["name"].asText(), "rows" to bed["rows"].asText(), "columns" to bed["columns"].asText())
        }
        assertThat(listed).containsAll(*table.asMaps().toTypedArray())
    }

    @Then("every listed bed links to its own address")
    fun everyListedBedLinksToItsOwnAddress() {
        json["_embedded"]["beds"].forEach { bed ->
            assertThat(bed["_links"]["self"]["href"].asText()).isEqualTo("/beds/${bed["id"].asText()}")
        }
    }

    @Then("the answer offers the form {string}")
    fun theAnswerOffersTheForm(form: String) {
        assertThat(json["_forms"].has(form)).isTrue()
    }

    @Then("the page links the beds {string} to their bed pages")
    fun thePageLinksTheBeds(names: String) {
        val links = Jsoup.parse(answer.body()).select("a.bed-link").associate { it.text() to it.attr("href") }
        names.split(" ").forEach { name ->
            assertThat(links[name]).isEqualTo("/beds/${world.bedId(name)}")
        }
    }

    @Then("at {string} of the bed {string} the agent's forms and the page's care buttons post to the same addresses")
    fun theAgentsFormsAndThePagesButtonsPostToTheSameAddresses(focus: String, bed: String) {
        val address = "/beds/${world.bedId(bed)}/focus/$focus"
        val html = Jsoup.parse(ask(address, "text/html").body())
        val agent = jacksonObjectMapper().readTree(ask(address, "application/hal+json").body())
        val buttons = html.select("#view .sheet form.care-action").map { it.attr("hx-post") }
        val forms = agent["_forms"].map { it["_links"]["target"]["href"].asText() }
        assertThat(buttons).isEqualTo(forms)
    }

    @Then("the answer is {string}")
    fun theAnswerIs(mediaType: String) {
        assertThat(answer.headers().firstValue("Content-Type").orElse("").substringBefore(";")).isEqualTo(mediaType)
    }

    @Then("the answer varies by {string}")
    fun theAnswerVariesBy(header: String) {
        val vary = answer.headers().allValues("Vary").flatMap { it.split(",") }.map { it.trim() }
        assertThat(vary).containsAll(header)
    }

    /**
     * The care buttons on the page are the agent's forms, and every move the agent is offered is
     * something the gardener can tap on the page.
     */
    @Then("a gardener and an agent at {string} of the bed {string} are offered the same care and moves")
    fun theSameCareAndMoves(focus: String, bed: String) {
        val address = "/beds/${world.bedId(bed)}/focus/$focus"
        val html = Jsoup.parse(ask(address, "text/html").body())
        val agent = jacksonObjectMapper().readTree(ask(address, "application/hal+json").body())

        val buttons = html.select("#view .sheet form.care-action").map { it.attr("data-action") }
        val forms = agent["_forms"].map { it["_links"]["target"]["href"].asText().substringAfterLast("/") }
        assertThat(buttons).isEqualTo(forms)

        val taps = html.select("#view [hx-get]").map { it.attr("hx-get").substringAfter("/focus/", "") }.toSet()
        val moves = agent["_links"].fields().asSequence()
            .filter { (rel, _) -> rel !in setOf("self", "bed", "journal") }
            .map { (_, link) -> link["href"].asText().substringAfter("/focus/") }
            .toList()
        assertThat(taps).containsAll(*moves.toTypedArray())
    }

    @Then("every link of the answer leads into {string}")
    fun everyLinkLeadsInto(prefix: String) {
        json["_links"].forEach { link -> assertThat(link["href"].asText().startsWith(prefix)).isTrue() }
    }

    @Then("the answer links {string} to the journal of {string}")
    fun theAnswerLinksToTheJournalOf(rel: String, focus: String) {
        assertThat(json["_links"][rel]["href"].asText().substringAfter("/journal?")).isEqualTo("focus=$focus")
    }

    @Then("the embedded recent events read:")
    fun theEmbeddedRecentEventsRead(table: DataTable) {
        val recent = json["_embedded"]["recent"].map { entry ->
            mapOf("type" to entry["type"].asText(), "cells" to entry["cells"].joinToString(" ") { it.asText() })
        }
        assertThat(recent).isEqualTo(table.asMaps())
    }

    @Then("{int} recent events are embedded")
    fun recentEventsAreEmbedded(count: Int) {
        assertThat(json["_embedded"]["recent"].size()).isEqualTo(count)
    }

    @Then("the answer links the journal folds {string}")
    fun theAnswerLinksTheJournalFolds(folds: String) {
        val rels = json["_links"].fieldNames().asSequence().filter { it.startsWith("by-") }.toList()
        assertThat(rels.joinToString(" ")).isEqualTo(folds)
    }

    @Then("the page-only {string} is not part of any link")
    fun thePageOnlyIsNotPartOfAnyLink(parameter: String) {
        val hrefs = json.findValues("href").map { it.asText() }
        hrefs.forEach { assertThat(it).doesNotContain("$parameter=") }
    }

    private fun ask(path: String, accept: String): HttpResponse<String> =
        page.send(HttpRequest.newBuilder(page.uri(path)).header("Accept", accept).GET())
}
