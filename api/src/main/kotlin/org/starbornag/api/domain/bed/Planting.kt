package org.starbornag.api.domain.bed

import com.fasterxml.jackson.annotation.JsonIgnore

/** What grows in a cell: a plant type and its cultivar, for example "tomato" and "Dark Galaxy". */
data class Planting(
    val plantType: String,
    val plantCultivar: String
) {
    @JsonIgnore
    fun isEmpty() = plantType == ""

    override fun toString() =
        if (isEmpty()) "" else "$plantType - $plantCultivar"
}

data class Harvest(
    val planting: Planting,
    val quantity: Int? = null,
    val weight: Double? = null
)
