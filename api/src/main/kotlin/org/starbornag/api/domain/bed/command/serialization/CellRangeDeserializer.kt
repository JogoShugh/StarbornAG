package org.starbornag.api.domain.bed.command.serialization

import org.starbornag.api.domain.bed.command.CellRange

class CellRangeDeserializer : StringToObjectDeserializer<CellRange>(
    CellRange::class.java,
    CellRange.Companion::fromString
)