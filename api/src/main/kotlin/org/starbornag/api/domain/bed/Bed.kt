package org.starbornag.api.domain.bed

import java.util.*

class Bed(
    val id: UUID,
    val name: String,
    val cells: List<String>,
    val dimensions: Dimensions
)