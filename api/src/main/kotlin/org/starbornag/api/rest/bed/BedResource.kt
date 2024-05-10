package org.starbornag.api.rest.bed

import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.RepresentationModel
import java.util.*

open class BedResource<T>(val id: UUID) : RepresentationModel<BedResource<T>>() {
    init {
        add(
            Links.of(
                Link.of("/api/beds/${id}").withSelfRel(),
                Link.of("/api/beds/${id}/plant").withRel("plant"),
                Link.of("/api/beds/${id}/water").withRel("water"),
                Link.of("/api/beds/${id}/fertilize").withRel("fertilize"),
                Link.of("/api/beds/${id}/history").withRel("history")
            )
        )
    }
}
