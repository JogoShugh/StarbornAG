package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import kotlinx.html.ul
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.application.bed.Beds
import org.starbornag.api.domain.bed.Bed
import java.util.*

/** The front door's HAL: where to go from here. */
data class FrontDoorResource(@get:JsonProperty("_links") val links: Map<String, Map<String, String>>)

/** One bed in the list of beds. */
data class BedEntry(
    val id: UUID,
    val name: String,
    val rows: Int,
    val columns: Int,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>
)

/** Every bed, each linking to its own address, and the form to prepare a new one. */
data class BedsResource(
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>,
    @get:JsonProperty("_embedded") val embedded: Map<String, List<BedEntry>>,
    @get:JsonProperty("_forms") val forms: Map<String, HalForm>
)

/**
 * The front door at "/" and the beds at "/beds", in both views: a browser gets the list of beds to
 * pick from, an agent gets HAL to follow from there.
 */
@RestController
class FrontDoorController(private val beds: Beds) {

    @GetMapping("/")
    suspend fun frontDoor(
        @RequestHeader(HttpHeaders.ACCEPT, required = false) accept: String?
    ): ResponseEntity<*> =
        if (Asking(accept, null).wantsHtml) {
            listPage()
        } else {
            hal(FrontDoorResource(mapOf("self" to href("/"), "beds" to href("/beds", "Every bed"))))
        }

    @GetMapping("/beds")
    suspend fun allBeds(
        @RequestHeader(HttpHeaders.ACCEPT, required = false) accept: String?
    ): ResponseEntity<*> =
        if (Asking(accept, null).wantsHtml) {
            listPage()
        } else {
            hal(
                BedsResource(
                    links = mapOf("self" to href("/beds"), "up" to href("/")),
                    embedded = mapOf("beds" to beds.all().map(::entry)),
                    forms = mapOf("prepare-bed" to HalSchemaForms.prepareBed())
                )
            )
        }

    private suspend fun listPage(): ResponseEntity<String> {
        val all = beds.all()
        val html = createHTML().html {
            lang = "en"
            head {
                meta(charset = "UTF-8")
                meta(name = "viewport", content = "width=device-width, initial-scale=1")
                title { +"Starborn beds" }
                link(rel = "stylesheet", href = "/styles.css")
            }
            body {
                main {
                    classes = setOf("front-door")
                    h1 { +"Beds" }
                    ul {
                        classes = setOf("bed-list")
                        all.forEach { bed ->
                            li {
                                a(href = "/beds/${bed.id}") {
                                    classes = setOf("bed-link")
                                    +bed.name
                                }
                                span {
                                    classes = setOf("bed-size")
                                    +"${bed.rows.size} rows × ${bed.rows.firstOrNull()?.size ?: 0} columns"
                                }
                            }
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok().contentType(Asking.HTML).header(HttpHeaders.VARY, HttpHeaders.ACCEPT).body(html)
    }

    private fun entry(bed: Bed) = BedEntry(
        bed.id, bed.name, bed.rows.size, bed.rows.firstOrNull()?.size ?: 0,
        mapOf("self" to href("/beds/${bed.id}", bed.name))
    )

    private fun <T> hal(body: T): ResponseEntity<T> = ResponseEntity.ok()
        .contentType(Asking.HAL)
        .header(HttpHeaders.LINK, BedResources.PROFILE_LINK)
        .header(HttpHeaders.VARY, HttpHeaders.ACCEPT)
        .body(body)

    private fun href(path: String, title: String? = null) =
        if (title == null) mapOf("href" to path) else mapOf("href" to path, "title" to title)
}
