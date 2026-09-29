package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.RepresentationModel
import java.util.*

/**
 * A bed as HAL with HAL Schema Forms: `_links` to navigate, and `_forms` for the actions that are
 * possible right now (see HalSchemaForms). A missing form means the action is not possible.
 */
open class BedResource<T>(val id: UUID) : RepresentationModel<BedResource<T>>() {

    init {
        add(
            Links.of(
                Link.of("/api/beds/${id}").withSelfRel(),
                Link.of("/api/beds/${id}/plant", "plant"),
                Link.of("/api/beds/${id}/water").withRel("water"),
                Link.of("/api/beds/${id}/fertilize").withRel("fertilize"),
                Link.of("/api/beds/${id}/mulch").withRel("mulch"),
                Link.of("/api/beds/${id}/harvest").withRel("harvest"),
                Link.of("/api/beds/${id}/history").withRel("history")
            )
        )
    }

    @get:JsonProperty("_forms")
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val forms: MutableMap<String, HalForm> = linkedMapOf()
}
