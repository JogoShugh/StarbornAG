package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*

@Component
class BedCommandMapper(
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val pathToCommandTypeMap = mapOf(
            "plant" to CellCommand.PlantSeedling::class,
            "fertilize" to CellCommand.Fertilize::class,
            "water" to CellCommand.Water::class,
            "mulch" to CellCommand.Mulch::class,
            "harvest" to CellCommand.Harvest::class
        )
    }

    fun convertCommand(action: String, commandData: Any): BedCommand {
        val commandType = pathToCommandTypeMap[action] ?: throw IllegalArgumentException("Unsupported action: $action")
        val value = if (commandData is String) commandData else objectMapper.writeValueAsString(commandData)
        return objectMapper.readValue(value, commandType.java)
    }
}
