package org.starbornag.api.domain.bed

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import org.starbornag.api.domain.bed.RelativeDatesDslTimePeriod.*

private fun relativeDate(value: Int, unit: ChronoUnit): Date =
    Date.from(ZonedDateTime.now().minus(value.toLong(), unit).toInstant())

sealed class RelativeDatesDslTimePeriod {
    data class Months(val value: Int) : RelativeDatesDslTimePeriod() {
        val ago: Date get() = relativeDate(this.value, ChronoUnit.MONTHS)
    }

    data class Days(val value: Int): RelativeDatesDslTimePeriod()  {
        val ago: Date get() = relativeDate(this.value, ChronoUnit.DAYS)
    }

    data class Weeks(val value: Int) : RelativeDatesDslTimePeriod() {
        val ago: Date get() = relativeDate(this.value, ChronoUnit.WEEKS)
    }
}

val Int.months get() = Months(this)
val Int.days get() = Days(this)
val Int.weeks get() = Weeks(this)
val now get() = 0.days.ago