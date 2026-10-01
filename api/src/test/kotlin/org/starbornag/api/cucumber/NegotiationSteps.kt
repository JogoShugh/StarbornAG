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

    /** An absolute address, or "Mars:/focus/cell/B2" for an address inside the bed named Mars. */
    @When("a client asks for {string} accepting {string}")
    fun aClientAsksForTheAddress(address: String, accept: String) {
        val inBed = Regex("^([^/:]+):(/.*)$").find(address)
        val path = inBed?.let { "/beds/${world.bedId(it.groupValues[1])}${it.groupValues[2]}" } ?: address
        answer = ask(path, accept)
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
        val forms = careForms(agent).map { it["_links"]["target"]["href"].asText() }
        assertThat(buttons).isEqualTo(forms)
    }

    @Then("the form {string} in the answer requires {string}")
    fun theFormInTheAnswerRequires(form: String, fields: String) {
        val required = json["_forms"][form]["schema"]["required"].map { it.asText() }.sorted()
        assertThat(required.joinToString(" ")).isEqualTo(fields)
    }

    @Then("the form {string} in the answer requires {string} of its {string}")
    fun theFormInTheAnswerRequiresOf(form: String, fields: String, property: String) {
        val nested = json["_forms"][form]["schema"]["properties"][property]
        assertThat(nested["required"].map { it.asText() }.sorted().joinToString(" ")).isEqualTo(fields)
    }

    /** Posts the body to the form's own target, with the form's method and content type. */
    @When("an agent submits the form {string} from the answer with {string}")
    fun anAgentSubmitsTheForm(form: String, body: String) {
        val theForm = json["_forms"][form]
        answer = page.send(
            HttpRequest.newBuilder(page.uri(theForm["_links"]["target"]["href"].asText()))
                .header("Content-Type", theForm["contentType"].asText())
                .header("Accept", "application/hal+json")
                .method(theForm["method"].asText(), HttpRequest.BodyPublishers.ofString(body))
        )
    }

    @Then("the answer status is {int}")
    fun theAnswerStatusIs(status: Int) {
        assertThat(answer.statusCode()).isEqualTo(status)
    }

    @Then("the bed at the answer's Location is named {string}")
    fun theBedAtTheLocationIsNamed(name: String) {
        aClientAsksForTheAddress(answer.headers().firstValue("Location").orElseThrow(), "application/hal+json")
        assertThat((json["name"] ?: json["bedName"]).asText()).isEqualTo(name)
    }

    @Then("the forms are titled {string}")
    fun theFormsAreTitled(titles: String) {
        val named = json["_forms"].joinToString(", ") { it["schema"]["title"]?.asText() ?: "(untitled)" }
        assertThat(named).isEqualTo(titles)
    }

    @Then("every field of every form has a title and a description")
    fun everyFieldHasATitleAndADescription() {
        val lacking = json["_forms"].fields().asSequence().flatMap { (id, form) ->
            form["schema"]["properties"].fields().asSequence()
                .filter { (_, field) -> !field.has("title") || !field.has("description") }
                .map { (name, _) -> "$id.$name" }
        }.toList()
        assertThat(lacking).isEqualTo(emptyList())
    }

    @Then("the field {string} of the form {string} is described as {string}")
    fun theFieldIsDescribedAs(field: String, form: String, description: String) {
        assertThat(json["_forms"][form]["schema"]["properties"][field]["description"].asText()).isEqualTo(description)
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
        val forms = careForms(agent).map { it["_links"]["target"]["href"].asText().substringAfterLast("/") }
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

    @Then("the answer offers these GET forms:")
    fun theAnswerOffersTheseGetForms(table: DataTable) {
        val base = json["_links"]["bed"]["href"].asText()
        val offered = json["_forms"].fields().asSequence()
            .filter { (_, form) -> form["method"].asText() == "GET" }
            .map { (id, form) ->
                mapOf(
                    "form" to id,
                    "target" to form["_links"]["target"]["href"].asText().removePrefix(base),
                    "fields" to form["schema"]["properties"].fieldNames().asSequence().joinToString(" ")
                )
            }.toList()
        assertThat(offered).isEqualTo(table.asMaps())
    }

    @Then("the form {string} lets {string} be one of {string}")
    fun theFormLetsBeOneOf(form: String, field: String, values: String) {
        val allowed = json["_forms"][form]["schema"]["properties"][field]["enum"].map { it.asText() }
        assertThat(allowed.joinToString(" ")).isEqualTo(values)
    }

    @Then("the form {string} lets {string} be at least {int}")
    fun theFormLetsBeAtLeast(form: String, field: String, minimum: Int) {
        val property = json["_forms"][form]["schema"]["properties"][field]
        assertThat(property["type"].asText() to property["minimum"].asInt()).isEqualTo("integer" to minimum)
    }

    @Then("the form {string} lets {string} run from {int} to {int}")
    fun theFormLetsRunFrom(form: String, field: String, first: Int, last: Int) {
        val property = json["_forms"][form]["schema"]["properties"][field]
        assertThat(property["minimum"].asInt() to property["maximum"].asInt()).isEqualTo(first to last)
    }

    /** Fills the form's templated target from "name=value" pairs, as the spec says, and follows it. */
    @When("an agent fills the GET form {string} at {string} of the bed {string} with {string}")
    fun anAgentFillsTheGetForm(form: String, from: String, bed: String, values: String) {
        aClientAsksFor("/focus/$from", bed, "application/hal+json")
        val target = json["_forms"][form]["_links"]["target"]
        assertThat(target["templated"].asBoolean()).isTrue()
        val variables = values.split(" ").associate { it.substringBefore("=") to it.substringAfter("=") }
        aClientAsksForTheAddress(expand(target["href"].asText(), variables), "application/hal+json")
    }

    @Then("the agent stands on {string}")
    fun theAgentStandsOn(label: String) {
        assertThat(json["focus"].asText()).isEqualTo(label)
    }

    /** The gardener's taps on the page against the agent's links plus every place its GET forms lead to. */
    @Then("at {string} of the bed {string} a gardener and an agent can reach the same places")
    fun theSamePlacesBothWays(focus: String, bed: String) {
        val bedAddress = "/beds/${world.bedId(bed)}"
        val address = "$bedAddress/focus/$focus"
        val html = Jsoup.parse(ask(address, "text/html").body())
        val agent = jacksonObjectMapper().readTree(ask(address, "application/hal+json").body())
        val taps = html.select("#view [hx-get]").map { place(it.attr("hx-get"), bedAddress) }.toSet()
        val links = agent["_links"].map { place(it["href"].asText(), bedAddress) }
        val reachable = (links + agent["_forms"].flatMap { expandAll(it) }.map { place(it, bedAddress) }).toSet()
        assertThat(reachable.sorted()).isEqualTo(taps.sorted())
    }

    /** The care the agent can post (navigation forms are GET). */
    private fun careForms(agent: JsonNode) = agent["_forms"].filter { it["method"].asText() == "POST" }

    /** Every address a bounded GET form can lead to: each field over its enum or its minimum to maximum. */
    private fun expandAll(form: JsonNode): List<String> {
        val target = form["_links"]["target"]
        val navigable = form["method"].asText() == "GET" && target.path("templated").asBoolean()
        val choices = form["schema"]["properties"].fields().asSequence().map { (name, property) ->
            name to when {
                property.has("enum") -> property["enum"].map { it.asText() }
                property.has("minimum") && property.has("maximum") ->
                    (property["minimum"].asInt()..property["maximum"].asInt()).map { it.toString() }
                else -> null // unbounded, such as how many recent events: not a place
            }
        }.toList()
        if (!navigable || choices.any { it.second == null }) return emptyList()
        return choices.fold(listOf(emptyMap<String, String>())) { sets, (name, values) ->
            sets.flatMap { set -> values!!.map { set + (name to it) } }
        }.map { expand(target["href"].asText(), it) }
    }

    /** RFC 6570 simple string expansion and form-style query expansion ({?a,b}), which these templates use. */
    private fun expand(template: String, variables: Map<String, String>): String =
        Regex("\\{(\\??)([\\w,]+)}").replace(template) { match ->
            val names = match.groupValues[2].split(",")
            if (match.groupValues[1] == "?") {
                names.filter { it in variables }.joinToString("&") { "$it=${variables.getValue(it)}" }
                    .let { if (it.isEmpty()) "" else "?$it" }
            } else {
                names.joinToString(",") { variables[it].orEmpty() }
            }
        }

    /** One spelling per place: the bed is its whole-bed focus, and page-only journal state is dropped. */
    private fun place(href: String, bedAddress: String): String = href
        .let { if (it == bedAddress) "$bedAddress/focus/bed" else it }
        .replace(Regex("[?&]size=[^&]*"), "").replace(Regex("[?&]by=cell(?=&|$)"), "")
        .replace("journal&", "journal?")

    private fun ask(path: String, accept: String): HttpResponse<String> =
        page.send(HttpRequest.newBuilder(page.uri(path)).header("Accept", accept).GET())
}
