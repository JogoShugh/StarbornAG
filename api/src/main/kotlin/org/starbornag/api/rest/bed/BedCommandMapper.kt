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
            "plant" to PlantSeedlingCommand::class,
            "fertilize" to FertilizeCommand::class,
            "water" to WaterCommand::class,
            "harvest" to HarvestCommand::class
        )
    }

    fun convertCommand(action: String, commandData: Any): BedCommand {
        val commandType = pathToCommandTypeMap[action] ?: throw IllegalArgumentException("Unsupported action: $action")
        return objectMapper.readValue(objectMapper.writeValueAsString(commandData), commandType.java)
    }
}
