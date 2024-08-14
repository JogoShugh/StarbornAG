package org.starbornag.api.domain.bed.command.serialization

//import kotlinx.serialization.json.Json
import org.starbornag.api.domain.bed.command.CellPosition

//class CellPositionDeserializer : StringToObjectDeserializer<CellPosition>(
//    CellPosition::class.java,
//    CellPosition.Companion::fromString, { node ->
//        // TODO clean this up:
//        val row = node.get("row")?.asInt()!!
//        val column = node.get("column")?.asInt()!!
//        CellPosition(row, column)
//    }
//)

class CellPositionDeserializer :
    StringToObjectDeserializerWithDefaultFallback<CellPosition>(
        CellPosition::class.java,
        CellPosition.Companion::fromString,
        { input ->
            CellPosition(1, 2)
            //Json.decodeFromString<CellPosition>(input)
        }
    )
