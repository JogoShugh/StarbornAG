package org.starbornag.api.rest.bed

import org.springframework.http.MediaType

/**
 * What a client asked for. A browser lists text/html first; an agent, curl or a client that says
 * nothing gets HAL. htmx requests are always the page's own.
 */
internal class Asking(accept: String?, htmx: String?, val recent: Int = FocusResource.RECENT) {
    val htmx = htmx != null
    val wantsHtml = this.htmx || prefersHtml(accept)

    private fun prefersHtml(accept: String?): Boolean {
        val types = runCatching { MediaType.parseMediaTypes(accept) }.getOrDefault(emptyList())
        val best = types.withIndex().maxWithOrNull(
            compareBy<IndexedValue<MediaType>> { it.value.qualityValue }.thenByDescending { it.index }
        )
        return best != null && best.value.includes(MediaType.TEXT_HTML) && !best.value.isWildcardType
    }

    companion object {
        const val HX_REQUEST = "HX-Request"

        /** Plant and care icons are emoji: without a charset, browsers fall back to Latin-1. */
        val HTML = MediaType(MediaType.TEXT_HTML, Charsets.UTF_8)
        val HAL = MediaType("application", "hal+json")
    }
}
