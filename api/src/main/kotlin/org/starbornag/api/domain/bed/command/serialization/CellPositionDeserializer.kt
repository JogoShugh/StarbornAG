package org.starbornag.api.domain.bed.command.serialization

import org.starbornag.api.domain.bed.command.CellPosition

class CellPositionDeserializer :
    StringToObjectDeserializer<CellPosition>(
        CellPosition::class.java,
        CellPosition.Companion::fromString
    )
