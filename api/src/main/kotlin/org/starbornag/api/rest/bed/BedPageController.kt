package org.starbornag.api.rest.bed

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
 * The bed page at /beds/{id} and at every focus address (/beds/{id}/focus/cell/B4, row/B, column/4,
 * bed). A plain request gets the whole page, so reloading or sharing a focus works; an htmx request
 * gets only the view.
 */
@RestController
class BedPageController(
    private val resources: BedResources,
    private val bedCells: BedCells,
    private val bedCommandMapper: BedCommandMapper
) {
    @GetMapping("/beds/{bedId}")
    suspend fun bed(@PathVariable bedId: UUID, @RequestHeader("HX-Request", required = false) htmx: String?) =
        render(bedId, "bed", htmx != null)

    @GetMapping("/beds/{bedId}/focus/bed")
    suspend fun wholeBed(@PathVariable bedId: UUID, @RequestHeader("HX-Request", required = false) htmx: String?) =
        render(bedId, "bed", htmx != null)

    @GetMapping("/beds/{bedId}/focus/{scope}/{ref}")
    suspend fun focus(
        @PathVariable bedId: UUID,
        @PathVariable scope: String,
        @PathVariable ref: String,
        @RequestHeader("HX-Request", required = false) htmx: String?
    ) = render(bedId, "$scope/$ref", htmx != null)

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

    private suspend fun render(bedId: UUID, path: String, fragmentOnly: Boolean): ResponseEntity<String> {
        val focus = resources.focus(bedId, path)
        val bed = resources.currentState(bedId)
        val html = if (fragmentOnly) BedPage.fragment(bed, focus) else BedPage.page(bed, focus)
        return ResponseEntity.ok().contentType(HTML).body(html)
    }

    private companion object {
        /** Plant and care icons are emoji: without a charset, browsers fall back to Latin-1. */
        val HTML = MediaType(MediaType.TEXT_HTML, Charsets.UTF_8)
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
