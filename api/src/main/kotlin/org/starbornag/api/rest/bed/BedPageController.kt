package org.starbornag.api.rest.bed

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.application.bed.BedCells
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand
import java.time.Instant
import java.util.*

/**
 * The bed at /beds/{id}, every focus address (/beds/{id}/focus/cell/B4, row/B, column/4, bed) and
 * the journal (/beds/{id}/journal), each in two views of the same resource: a browser gets the page
 * (an htmx request only the view), an agent gets HAL Schema Forms. The Accept header decides.
 */
@RestController
class BedPageController(
    private val resources: BedResources,
    private val bedCells: BedCells,
    private val bedCommandMapper: BedCommandMapper
) {
    @GetMapping("/beds/{bedId}", "/beds/{bedId}/focus/bed")
    suspend fun wholeBed(
        @PathVariable bedId: UUID,
        @RequestParam(defaultValue = "${FocusResource.RECENT}") recent: Int,
        @RequestHeader(HttpHeaders.ACCEPT, required = false) accept: String?,
        @RequestHeader(HX_REQUEST, required = false) htmx: String?
    ) = focusOrPage(bedId, "bed", Asking(accept, htmx, recent))

    @GetMapping("/beds/{bedId}/focus/{scope}/{ref}")
    @Suppress("LongParameterList")
    suspend fun focus(
        @PathVariable bedId: UUID,
        @PathVariable scope: String,
        @PathVariable ref: String,
        @RequestParam(defaultValue = "${FocusResource.RECENT}") recent: Int,
        @RequestHeader(HttpHeaders.ACCEPT, required = false) accept: String?,
        @RequestHeader(HX_REQUEST, required = false) htmx: String?
    ) = focusOrPage(bedId, "$scope/$ref", Asking(accept, htmx, recent))

    /**
     * The journal of the focus, folded [by] cell, row, column or time. How far it is open ([size]) is
     * the page's own business; agents ignore it.
     */
    @GetMapping("/beds/{bedId}/journal")
    @Suppress("LongParameterList")
    suspend fun journal(
        @PathVariable bedId: UUID,
        @RequestParam(defaultValue = "bed") focus: String,
        @RequestParam(defaultValue = "cell") by: String,
        @RequestParam(defaultValue = "half") size: String,
        @RequestHeader(HttpHeaders.ACCEPT, required = false) accept: String?,
        @RequestHeader(HX_REQUEST, required = false) htmx: String?
    ): ResponseEntity<*> {
        val asking = Asking(accept, htmx)
        return if (asking.wantsHtml) {
            page(bedId, focus, asking, JournalChoice.of(by, size))
        } else {
            hal(resources.journal(bedId, focus, JournalChoice.of(by, size).by))
        }
    }

    @PostMapping("/beds/{bedId}/focus/bed/{action}")
    suspend fun careForWholeBed(
        @PathVariable bedId: UUID,
        @PathVariable action: String,
        @RequestParam fields: MultiValueMap<String, String>
    ) = care(bedId, "bed", action, fields)

    @PostMapping("/beds/{bedId}/focus/{scope}/{ref}/{action}")
    suspend fun careForFocus(
        @PathVariable bedId: UUID,
        @PathVariable scope: String,
        @PathVariable ref: String,
        @PathVariable action: String,
        @RequestParam fields: MultiValueMap<String, String>
    ) = care(bedId, "$scope/$ref", action, fields)

    private suspend fun focusOrPage(bedId: UUID, path: String, asking: Asking): ResponseEntity<*> =
        if (asking.wantsHtml) page(bedId, path, asking) else hal(resources.focus(bedId, path, asking.recent))

    private suspend fun page(
        bedId: UUID,
        path: String,
        asking: Asking,
        journal: JournalChoice? = null
    ): ResponseEntity<String> {
        val focus = resources.focus(bedId, path)
        val bed = resources.currentState(bedId)
        val html = if (asking.htmx) {
            BedPage.fragment(bed, focus, journal = journal)
        } else {
            BedPage.page(bed, focus, journal = journal)
        }
        return ResponseEntity.ok().contentType(HTML).body(html)
    }

    private fun <T> hal(body: T): ResponseEntity<T> = ResponseEntity.ok()
        .contentType(HAL)
        .header(HttpHeaders.LINK, BedResources.PROFILE_LINK)
        .body(body)

    /**
     * What the client asked for. A browser lists text/html first; an agent, curl or a client that
     * says nothing gets HAL. htmx requests are always the page's own.
     */
    private class Asking(accept: String?, htmx: String?, val recent: Int = FocusResource.RECENT) {
        val htmx = htmx != null
        val wantsHtml = this.htmx || prefersHtml(accept)

        private fun prefersHtml(accept: String?): Boolean {
            val types = runCatching { MediaType.parseMediaTypes(accept) }.getOrDefault(emptyList())
            val best = types.withIndex().maxWithOrNull(
                compareBy<IndexedValue<MediaType>> { it.value.qualityValue }.thenByDescending { it.index }
            )
            return best != null && best.value.includes(MediaType.TEXT_HTML) && !best.value.isWildcardType
        }
    }

    internal companion object {
        const val HX_REQUEST = "HX-Request"

        /** Plant and care icons are emoji: without a charset, browsers fall back to Latin-1. */
        val HTML = MediaType(MediaType.TEXT_HTML, Charsets.UTF_8)
        val HAL = MediaType("application", "hal+json")
    }

    /**
     * Records care on exactly the cells in focus, then answers with the refreshed view. A care the
     * soil rules refuse (planting a planted cell, harvesting bare soil) shows as a message instead.
     */
    private suspend fun care(
        bedId: UUID,
        path: String,
        action: String,
        fields: MultiValueMap<String, String>
    ): ResponseEntity<String> {
        val focus = resources.focus(bedId, path)
        val payload = fields.toSingleValueMap() +
            mapOf("bedId" to bedId.toString(), "started" to Instant.now().toString(), "location" to focus.location)
        val message = try {
            bedCells.handle(bedCommandMapper.convertCommand(action, payload) as CellCommand)
            null
        } catch (e: IllegalStateException) {
            e.message
        }
        val html = BedPage.fragment(resources.currentState(bedId), resources.focus(bedId, path), message)
        return ResponseEntity.ok().contentType(HTML).body(html)
    }
}
