package org.starbornag.api.cucumber

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.java.ParameterType
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
 * with the HX-Request header, and the answer is swapped into the page like htmx would swap it.
 */
class PageSteps(private val world: GardenWorld) {

    private val http = HttpClient.newHttpClient()
    private lateinit var page: Document
    private var addressBar: String = ""

    @ParameterType("the cell [A-Z]\\d+|the row [A-Z]|the column \\d+|the bed name")
    fun target(text: String): String = when {
        text.startsWith("the cell") -> "#cell-${text.substringAfterLast(" ")}"
        text.startsWith("the row") -> ".row-label[data-row=${text.substringAfterLast(" ")}]"
        text.startsWith("the column") -> ".col-label[data-column=${text.substringAfterLast(" ")}]"
        else -> "h1.garden-name"
    }

    @When("a gardener opens the bed page of {string}")
    fun aGardenerOpensTheBedPage(bed: String) = open("/beds/${world.bedId(bed)}")

    @Given("a gardener has opened the bed page of {string}")
    fun aGardenerHasOpenedTheBedPage(bed: String) = aGardenerOpensTheBedPage(bed)

    @When("a gardener opens the focus {string} of the bed page of {string}")
    fun aGardenerOpensTheFocus(focus: String, bed: String) = open("/beds/${world.bedId(bed)}/focus/$focus")

    @Given("a gardener has opened the focus {string} of the bed page of {string}")
    fun aGardenerHasOpenedTheFocus(focus: String, bed: String) = aGardenerOpensTheFocus(focus, bed)

    @When("the gardener taps {target}")
    fun theGardenerTaps(selector: String) = tap(page.selectFirst(selector)!!)

    @When("the gardener taps the pad's {string}")
    fun theGardenerTapsThePad(move: String) = tap(page.selectFirst(".pad-move[data-move=$move]")!!)

    @When("the gardener taps {string} in the action bar")
    fun theGardenerTapsInTheActionBar(action: String) {
        val form = page.selectFirst("form.care-action[data-action=$action]")!!
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

    @Then("the page shows {int} rows labelled {string} and {int} columns labelled {string}")
    fun thePageShowsRowsAndColumns(rows: Int, rowLabels: String, columns: Int, columnLabels: String) {
        assertThat(page.select(".row-label").map { it.text() }).isEqualTo(rowLabels.split(" "))
        assertThat(page.select(".col-label").map { it.text() }).isEqualTo(columnLabels.split(" "))
        assertThat(page.select(".grid-item").size).isEqualTo(rows * columns)
    }

    @Then("the focus panel shows {string}")
    fun theFocusPanelShows(label: String) {
        assertThat(page.selectFirst("#focus-panel .focus-label")!!.text()).isEqualTo(label)
    }

    /** Cell labels from the highlight's selectors, or "all" when every cell is highlighted. */
    @Then("the highlighted cells are {string}")
    fun theHighlightedCellsAre(cells: String) {
        val css = page.selectFirst("style#focus-style")!!.data()
        val highlighted = if (css.startsWith(".grid-item")) "all" else
            Regex("#cell-([A-Z]\\d+)").findAll(css).joinToString(" ") { it.groupValues[1] }
        assertThat(highlighted).isEqualTo(cells)
    }

    @Then("the address bar shows the focus {string}")
    fun theAddressBarShows(path: String) {
        assertThat(addressBar.substringAfter("/focus/")).isEqualTo(path)
    }

    @Then("the pad offers {string}")
    fun thePadOffers(moves: String) {
        val offered = page.select(".pad-move").map { it.attr("data-move") }
        assertThat(offered.sorted()).isEqualTo(moves.split(" ").filter { it.isNotEmpty() }.sorted())
    }

    @Then("the action bar offers {string}")
    fun theActionBarOffers(actions: String) {
        assertThat(page.select("form.care-action").map { it.attr("data-action") }).isEqualTo(actions.split(" "))
    }

    @Then("the action {string} asks for {string}")
    fun theActionAsksFor(action: String, fields: String) {
        assertThat(fieldsOf(action)).isEqualTo(fields.split(" "))
    }

    @Then("the action {string} asks for nothing")
    fun theActionAsksForNothing(action: String) {
        assertThat(fieldsOf(action)).isEqualTo(emptyList())
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

    private fun fieldsOf(action: String): List<String> =
        page.selectFirst("form.care-action[data-action=$action]")!!
            .select("input[name], select[name]").map { it.attr("name") }

    private fun open(path: String) {
        val response = send(HttpRequest.newBuilder(uri(path)).GET())
        assertThat(response.statusCode()).isEqualTo(200)
        page = Jsoup.parse(response.body())
        addressBar = path
    }

    /** Follows the element's hx-get like htmx, and records the pushed address when hx-push-url is set. */
    private fun tap(element: Element) {
        val path = element.attr("hx-get")
        swapIn(send(HttpRequest.newBuilder(uri(path)).header("HX-Request", "true").GET()))
        if (element.attr("hx-push-url") == "true") addressBar = path
    }

    /** Swaps the answered panel in place of the page's panel, and the out-of-band highlight too. */
    private fun swapIn(response: HttpResponse<String>) {
        assertThat(response.statusCode()).isEqualTo(200)
        val fragment = Jsoup.parseBodyFragment(response.body()).body()
        page.selectFirst("#focus-panel")!!.replaceWith(fragment.selectFirst("#focus-panel")!!)
        fragment.selectFirst("style#focus-style[hx-swap-oob]")
            ?.let { page.selectFirst("style#focus-style")!!.replaceWith(it) }
    }

    private fun send(request: HttpRequest.Builder): HttpResponse<String> =
        http.send(request.build(), HttpResponse.BodyHandlers.ofString())

    private fun uri(path: String) = URI.create(TestApplication.baseUrl + path)
}
