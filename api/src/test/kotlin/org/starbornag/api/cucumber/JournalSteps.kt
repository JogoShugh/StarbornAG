package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.net.http.HttpRequest

/**
 * Drives the bed journal on the page like PageSteps does: the handle, the journal's own controls
 * (Full, Map, Close), the tabs and the cards are tapped by following their hx-get.
 */
class JournalSteps(private val world: GardenWorld, private val page: PageSteps) {

    private val journal get() = page.view.selectFirst(".journal")

    @When("the gardener opens the journal")
    fun theGardenerOpensTheJournal() = page.tap(page.view.selectFirst(".sheet .handle")!!)

    @Given("a gardener has opened the journal of the bed {string}")
    fun aGardenerHasOpenedTheJournal(bed: String) {
        page.aGardenerOpensTheBedPage(bed)
        theGardenerOpensTheJournal()
    }

    @When("the gardener taps the journal tab {string}")
    fun theGardenerTapsTheJournalTab(tab: String) =
        page.tap(journal!!.select(".tabs .tab").first { it.text() == tab })

    @When("the gardener taps {string} on the journal")
    fun theGardenerTapsOnTheJournal(control: String) =
        page.tap(page.view.selectFirst("[data-control=${control.lowercase()}]")!!)

    @When("the gardener taps the cell {string} in the journal")
    fun theGardenerTapsTheCellInTheJournal(cell: String) =
        page.tap(journal!!.selectFirst(".cell-card[data-cell=$cell]")!!)

    @When("a gardener opens the journal address {string} of the bed {string}")
    fun aGardenerOpensTheJournalAddress(query: String, bed: String) =
        page.open("/beds/${world.bedId(bed)}/journal?$query")

    @Then("the journal is open {string} by {string}")
    fun theJournalIsOpen(size: String, by: String) {
        assertThat(page.view.attr("data-journal")).isEqualTo(size)
        assertThat(journal!!.attr("data-by")).isEqualTo(by)
    }

    @Then("the journal is closed")
    fun theJournalIsClosed() {
        assertThat(journal).isNull()
        assertThat(page.view.selectFirst(".sheet .handle")).isNotNull()
    }

    @Then("the map is hidden")
    fun theMapIsHidden() {
        assertThat(page.view.selectFirst(".map")).isNull()
    }

    @Then("the journal offers the tabs {string}")
    fun theJournalOffersTheTabs(tabs: String) {
        assertThat(journal!!.select(".tabs .tab").joinToString(" ") { it.text() }).isEqualTo(tabs)
    }

    @Then("the journal's totals read:")
    fun theJournalsTotalsRead(table: DataTable) {
        val totals = table.asMaps().single().keys.associateWith { name ->
            journal!!.selectFirst("[data-total=${name.replace(' ', '-')}] .value")!!.text()
        }
        assertThat(totals).isEqualTo(table.asMaps().single())
    }

    @Then("the journal's first cells read:")
    fun theJournalsFirstCellsRead(table: DataTable) {
        val expected = table.asMaps().map { row -> row.mapValues { it.value.orEmpty() } }
        val cards = journal!!.select(".cell-card").take(expected.size).map { card ->
            mapOf(
                "cell" to card.attr("data-cell"),
                "plant" to card.selectFirst(".plant")!!.text(),
                "care" to card.selectFirst(".care")!!.text(),
                "events" to card.selectFirst(".events")!!.text(),
                "last" to card.selectFirst(".last")!!.text()
            )
        }
        assertThat(cards).isEqualTo(expected)
    }

    @Then("the journal lists the cells {string}")
    fun theJournalListsTheCells(cells: String) {
        assertThat(journal!!.select(".cell-card").joinToString(" ") { it.attr("data-cell") }).isEqualTo(cells)
    }

    @Then("the mini map outlines the cells {string}")
    fun theMiniMapOutlinesTheCells(cells: String) {
        val outlined = page.view.select(".mini-map .mini.in-focus").map { it.attr("data-cell") }
        assertThat(outlined.sorted().joinToString(" ")).isEqualTo(cells.split(" ").sorted().joinToString(" "))
    }

    @Then("the {word} {string} in the journal grows {string}")
    fun theLineGrows(kind: String, ref: String, grows: String) {
        assertThat(line(kind, ref).selectFirst(".grows")!!.text()).isEqualTo(grows)
    }

    @Then("the {word} {string} in the journal reads:")
    fun theLineReads(kind: String, ref: String, table: DataTable) {
        assertThat(commands(line(kind, ref))).isEqualTo(table.asMaps())
    }

    @Then("the journal's timeline reads:")
    fun theJournalsTimelineReads(table: DataTable) {
        assertThat(commands(journal!!.selectFirst(".timeline")!!)).isEqualTo(table.asMaps())
    }

    @Then("the page is a whole page with the journal open {string} by {string}")
    fun thePageIsAWholePageWithTheJournalOpen(size: String, by: String) {
        assertThat(page.page.selectFirst("head link[rel=stylesheet]")).isNotNull()
        theJournalIsOpen(size, by)
    }

    private var json = jacksonObjectMapper().createObjectNode()

    @When("a client reads the journal of the bed {string} by {string}")
    fun aClientReadsTheJournal(bed: String, by: String) {
        val response = page.send(HttpRequest.newBuilder(page.uri("/api/beds/${world.bedId(bed)}/journal?by=$by")).GET())
        assertThat(response.statusCode()).isEqualTo(200)
        json = jacksonObjectMapper().readTree(response.body()).deepCopy()
    }

    @Then("the journal's lines link to the focuses {string}")
    fun theJournalsLinesLinkToTheFocuses(focuses: String) {
        val links = json["lines"].map { it["_links"]["focus"]["href"].asText().substringAfter("/focus/") }
        assertThat(links.joinToString(" ")).isEqualTo(focuses)
    }

    @Then("the journal line {string} reports the commands:")
    fun theJournalLineReportsTheCommands(line: String, table: DataTable) {
        val commands = json["lines"].first { it["line"].asText() == line }["commands"].map { command ->
            mapOf("type" to command["type"].asText(), "cells" to command["cells"].joinToString(" ") { it.asText() })
        }
        assertThat(commands).isEqualTo(table.asMaps())
    }

    private fun commands(within: org.jsoup.nodes.Element) = within.select(".command").map {
        mapOf(
            "care" to it.selectFirst(".what")!!.text(),
            "cells" to it.selectFirst(".cells")!!.text(),
            "when" to it.selectFirst(".when")!!.text()
        )
    }

    private fun line(kind: String, ref: String) =
        journal!!.selectFirst(".line-summary[data-line=\"$kind/$ref\"]")
            ?: error("No $kind $ref in the journal: ${journal!!.select(".line-summary").map { it.attr("data-line") }}")
}
