package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.starbornag.api.testsupport.TestApplication
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Drives the bed page the way the browser does: a tap follows the element's own hx-get or hx-post,
 * with the HX-Request header, and the answer replaces the page's #view like htmx would swap it.
 */
class PageSteps(private val world: GardenWorld) {

    private val http = HttpClient.newHttpClient()
    lateinit var page: Document
        private set
    var addressBar: String = ""
    private var lastContentType: String = ""

    val view: Element get() = page.selectFirst("#view")!!

    @When("a gardener opens the bed page of {string}")
    fun aGardenerOpensTheBedPage(bed: String) = open("/beds/${world.bedId(bed)}")

    @Given("a gardener has opened the bed page of {string}")
    fun aGardenerHasOpenedTheBedPage(bed: String) = aGardenerOpensTheBedPage(bed)

    @When("a gardener opens the focus {string} of the bed page of {string}")
    fun aGardenerOpensTheFocus(focus: String, bed: String) = open("/beds/${world.bedId(bed)}/focus/$focus")

    @Given("a gardener has opened the focus {string} of the bed page of {string}")
    fun aGardenerHasOpenedTheFocus(focus: String, bed: String) = aGardenerOpensTheFocus(focus, bed)

    @When("the gardener taps the neighbor {string}")
    fun theGardenerTapsTheNeighbor(cell: String) = tap(view.selectFirst(".hood .slot[data-cell=$cell]")!!)

    @When("the gardener taps the breadcrumb {string}")
    fun theGardenerTapsTheBreadcrumb(crumb: String) =
        tap(view.select(".crumbs .crumb").first { it.text() == crumb })

    @When("the gardener taps {string} in the sheet")
    fun theGardenerTapsInTheSheet(action: String) {
        val form = view.selectFirst(".sheet form.care-action[data-action=$action]")!!
        val fields = form.select("input[name], select[name]").associate { field ->
            field.attr("name") to field.attr("value").ifEmpty { field.selectFirst("option")?.text() ?: "test" }
        }
        val body = fields.entries.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, Charsets.UTF_8)}" }
        swapIn(
            send(
                HttpRequest.newBuilder(uri(form.attr("hx-post")))
                    .header("HX-Request", "true")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
            )
        )
    }

    @Then("the view shows {int} rows labelled {string} and {int} columns labelled {string}")
    fun theViewShowsTheBed(rows: Int, rowLabels: String, columns: Int, columnLabels: String) {
        assertThat(view.select(".bed-map .row-label").map { it.text() }).isEqualTo(rowLabels.split(" "))
        assertThat(view.select(".bed-map .col-label").map { it.text() }).isEqualTo(columnLabels.split(" "))
        assertThat(view.select(".bed-map .tile").size).isEqualTo(rows * columns)
    }

    @Then("tapping these opens their focus:")
    fun tappingTheseOpensTheirFocus(table: DataTable) {
        table.asMaps().forEach { row ->
            val (kind, name) = row.getValue("tap").split(" ")
            val selector = when (kind) {
                "row" -> ".row-label[data-row=$name]"
                "column" -> ".col-label[data-column=$name]"
                else -> ".tile[data-cell=$name]"
            }
            assertThat(focusOf(view.selectFirst(selector)!!)).isEqualTo(row["opens"])
        }
    }

    @Then("the breadcrumb reads {string}")
    fun theBreadcrumbReads(crumbs: String) {
        assertThat(view.select(".crumbs .crumb").joinToString(" › ") { it.text() }).isEqualTo(crumbs)
    }

    @Then("the view shows the cells {string}")
    fun theViewShowsTheCells(cells: String) {
        assertThat(view.select(".line.in-focus .tile").joinToString(" ") { it.attr("data-cell") }).isEqualTo(cells)
    }

    /** A peeking row or column names the focus it leads to, or "edge" past the bed. */
    @Then("{string} peeks in before and {string} after")
    fun peeksIn(before: String, after: String) {
        assertThat(peek("before")).isEqualTo(before)
        assertThat(peek("after")).isEqualTo(after)
    }

    /** The nine slots in compass order (northwest to southeast): a cell label, or "edge" past the bed. */
    @Then("the neighborhood reads {string}")
    fun theNeighborhoodReads(slots: String) {
        val read = view.select(".hood .slot").map { if (it.hasClass("edge")) "edge" else it.attr("data-cell") }
        assertThat(read.joinToString(" ")).isEqualTo(slots)
    }

    @Then("the neighbor {string} sits {word} of the cell in landscape and {word} of it in portrait")
    fun theNeighborSits(cell: String, landscape: String, portrait: String) {
        val style = view.selectFirst(".hood .slot[data-cell=$cell]")!!.attr("style")
        fun v(name: String) = Regex("--$name:\\s*(\\d+)").find(style)!!.groupValues[1].toInt()
        assertThat(side(v("lr"), v("lc"))).isEqualTo(landscape)
        assertThat(side(v("pr"), v("pc"))).isEqualTo(portrait)
    }

    @Then("the address bar shows the focus {string}")
    fun theAddressBarShows(path: String) {
        assertThat(addressBar.substringAfter("/focus/")).isEqualTo(path)
    }

    @Then("the sheet offers {string}")
    fun theSheetOffers(actions: String) {
        assertThat(view.select(".sheet form.care-action").map { it.attr("data-action") }).isEqualTo(actions.split(" "))
    }

    @Then("the action {string} asks for {string}")
    fun theActionAsksFor(action: String, fields: String) {
        assertThat(fieldsOf(action)).isEqualTo(fields.split(" "))
    }

    @Then("the action {string} asks for nothing")
    fun theActionAsksForNothing(action: String) {
        assertThat(fieldsOf(action)).isEqualTo(emptyList())
    }

    @Then("the action {string} offers {string} to choose as {string}")
    fun theActionOffersToChoose(action: String, choices: String, field: String) {
        val select = view.selectFirst("form.care-action[data-action=$action] select[name=$field]")
        assertThat(select?.select("option")?.map { it.text() }?.joinToString(" ")).isEqualTo(choices)
    }

    @Then("the sheet's history reads:")
    fun theSheetsHistoryReads(table: DataTable) {
        val entries = view.select(".sheet .history li").map {
            mapOf("what" to it.selectFirst(".what")!!.text(), "when" to it.selectFirst(".when")!!.text())
        }
        assertThat(entries).isEqualTo(table.asMaps())
    }

    @Then("the sheet's history starts with {string}")
    fun theSheetsHistoryStartsWith(what: String) {
        assertThat(view.selectFirst(".sheet .history li .what")?.text()).isEqualTo(what)
    }

    /** "no history" when the sheet has no history list at all; otherwise the list's empty note. */
    @Then("the sheet's history shows {string}")
    fun theSheetsHistoryShows(shows: String) {
        val history = view.selectFirst(".sheet .history")
        assertThat(history?.text() ?: "no history").isEqualTo(shows)
    }

    @Then("the page offers a light and dark toggle")
    fun thePageOffersALightAndDarkToggle() {
        assertThat(page.selectFirst("button.theme-toggle")).isNotNull()
        assertThat(page.selectFirst("html")!!.hasAttr("data-theme")).isEqualTo(true)
    }

    /** Both the Content-Type header and the page's own <meta charset>, as a browser reads them. */
    @Then("the page declares the character set {string}")
    fun thePageDeclaresTheCharacterSet(charset: String) {
        assertThat(lastContentType.contains("charset=$charset", ignoreCase = true)).isEqualTo(true)
        assertThat(page.selectFirst("meta[charset]")?.attr("charset")?.uppercase()).isEqualTo(charset)
    }

    @Then("the last answer declares the character set {string}")
    fun theLastAnswerDeclaresTheCharacterSet(charset: String) {
        assertThat(lastContentType.contains("charset=$charset", ignoreCase = true)).isEqualTo(true)
    }

    @Then("the page is a whole page with the breadcrumb {string}")
    fun thePageIsAWholePage(crumbs: String) {
        assertThat(page.selectFirst("head link[rel=stylesheet]")).isNotNull()
        theBreadcrumbReads(crumbs)
    }

    @Then("the cells with a recorded {string} in the bed {string} are {string}")
    fun theCellsWithARecordedActionInTheBedAre(action: String, bed: String, cells: String) {
        val response = send(HttpRequest.newBuilder(uri("/api/beds/${world.bedId(bed)}")).GET())
        val rows = jacksonObjectMapper().readTree(response.body())["rows"]
        val recorded = rows.flatMapIndexed { r, row ->
            row["cells"].mapIndexedNotNull { c, cell ->
                "${r + 1}:${c + 1}".takeIf { action == "water" && !cell["lastWatering"].isNull }
            }
        }
        assertThat(recorded.joinToString(" ")).isEqualTo(cells)
    }

    private fun peek(side: String): String {
        val element = view.selectFirst(".peek[data-side=$side]")!!
        return if (element.hasClass("edge")) "edge" else focusOf(element)
    }

    /** The 3x3 slot at [row], [column] (1-based) seen from the center slot. */
    private fun side(row: Int, column: Int): String = when {
        row == 1 && column == 2 -> "above"
        row == 3 && column == 2 -> "below"
        row == 2 && column == 1 -> "left"
        row == 2 && column == 3 -> "right"
        else -> "diagonal"
    }

    fun focusOf(element: Element): String = element.attr("hx-get").substringAfter("/focus/")

    private fun fieldsOf(action: String): List<String> =
        view.selectFirst("form.care-action[data-action=$action]")!!
            .select("input[name], select[name]").map { it.attr("name") }

    /** Opens an address the way a browser does, asking for HTML. */
    fun open(path: String) {
        val response = send(HttpRequest.newBuilder(uri(path)).header("Accept", BROWSER_ACCEPT).GET())
        assertThat(response.statusCode()).isEqualTo(200)
        page = Jsoup.parse(response.body())
        addressBar = path
    }

    /** Follows the element's hx-get like htmx, and records the pushed address when hx-push-url is set. */
    fun tap(element: Element) {
        val path = element.attr("hx-get")
        swapIn(send(HttpRequest.newBuilder(uri(path)).header("HX-Request", "true").GET()))
        if (element.attr("hx-push-url") == "true") addressBar = path
    }

    /** Replaces the page's #view with the answered one, as hx-target="#view" hx-swap="outerHTML" does. */
    private fun swapIn(response: HttpResponse<String>) {
        assertThat(response.statusCode()).isEqualTo(200)
        val fragment = Jsoup.parseBodyFragment(response.body()).body()
        page.selectFirst("#view")!!.replaceWith(fragment.selectFirst("#view")!!)
    }

    fun send(request: HttpRequest.Builder): HttpResponse<String> =
        http.send(request.build(), HttpResponse.BodyHandlers.ofString())
            .also { lastContentType = it.headers().firstValue("Content-Type").orElse("") }

    fun uri(path: String) = URI.create(TestApplication.baseUrl + path)

    private companion object {
        const val BROWSER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    }
}
