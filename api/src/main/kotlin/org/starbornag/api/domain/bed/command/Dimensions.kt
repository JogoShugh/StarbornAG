package org.starbornag.api.domain.bed.command

data class Dimensions(
    val rows: Int,
    val columns: Int,
    val height: Int = 0
)