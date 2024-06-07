package org.starbornag.api.domain.bed.command

data class Dimensions(val length: Int,
                      val width: Int,
                      val height: Int = 0)