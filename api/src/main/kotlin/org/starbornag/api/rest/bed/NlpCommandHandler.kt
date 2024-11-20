package org.starbornag.api.rest.bed

import ch.rasc.sse.eventbus.SseEventBus
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
//import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.ResponseFormat
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.LogTimer.logNow
import org.starbornag.api.domain.bed.BedEventBus
import org.starbornag.api.domain.bed.BedRepository
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand.PlantSeedling
import reactor.core.publisher.Flux
import java.util.*

private fun getSystemPrompt(bedId: UUID) =
    """
        You should utilize the tools available to you to parse the supplied text to you
        and call them with the right JSON schema arguments. Do your best to map the parameters properly into the available JSON schema. Remember that bedId always will be ${bedId}.
        
        Remember that you need to figure out the distinction between "plantType" and "plantCultivar" in certain situations.
        
        For example, "Roma Grape Tomatoes" has a plantType of "tomato" and a plantCultivar of "Roma Grape".
        
        You need to also remember that if given a "sreadsheet" cell address, you treat the letter as the row, not the column.
        
        For example, B4 translates to row = 2, column = 4.
        
        Also remember there will be cases where you're asked to direct commands to one or more cells or one or more rows or columns.
        
        You should carefully map those requests utilizing the schema provided to you to make sure those multiplicity cases are handled well.
        
        Examples:
        
        "Planted asian cabbage in rows 1 and 2" -> location: { rows: [1,2] }
        "Plant dark galaxy tomato in rows 1, 2, and 3 and in column 4" -> location: { rows: [1,2,3], column: 4 }
        "Planted russet potatoes in columns 1 and 4 and in square 2,2" -> 
            location : { columns: [1,4], cell: { row: 2, column: 2 }
                             
        Keep in mind there may be a variety of locations specified in a single message and you need
        to support that. Here is a complex example:
        
        "Planted sugar beets in rows 1 and 2, column 4, and in cell E6" -> location: { rows: [1,2], column: 4, cell: { row:5, column: 6 }     
               
        Now the kicker: You must recognize when multiple distinct commands appear in a single message and produce an array of commands in a structure like { "commands": [ ... ] }.
        
        Examples: 
        
        "Planted sugar beets in rows 1 and 2, and column 4 and dark galaxy tomato in rows 3 and 6 and in square 5:10" -> {
            commands: [
                { bedId: "$bedId", plantType: "beet", plantCultivar: "sugar", location: { rows: [1,2], column: 4 } }
                { bedId: "$bedId", plantType: "tomato", plantCultivar: "dark galaxy", location: { rows: [3,6], cell: { row: 5, column: 10 } }
            ]
          }
        
        Please always remember any command will always have bedId : "$bedId" in it! Do not forget what I just said about
        there may be more than one command in a given message! 
        
        Good luck to you!
    """.trimIndent()

private fun ORIGgetSystemPrompt(bedId: UUID) =
    """
    You are a helpful assistant that generates a JSON array of actions for a garden bed. Each action should be represented as an object with two properties:
    
    * "action": A string representing the action to be taken (e.g., "plant", "water", "fertilize", "mulch").
    * "payload": An object containing the relevant parameters for the action, following the structure of the corresponding Kotlin data class.
    
    The available actions and their expected parameters are defined by the following Kotlin data classes where the bedId value is always going to be $bedId
    
    sealed class BedCommand : BedId {
        data class PrepareBed(
            override val bedId: UUID,
            val name: String,
            val dimensions: Dimensions,
            val cellBlockSize: Int = 1
        ) : BedCommand()
    
        sealed class CellCommand() : BedCommand(), BedId {
            abstract val location: CellsSelection?
    
            data class PlantSeedling(
                override val bedId: UUID,
                val started: Date,
                val plantType: String,
                val plantCultivar: String,
                override val location: CellsSelection? = null
            ) : CellCommand()
    
            data class Fertilize(
                override val bedId: UUID,
                val started: Date,
                val volume: Double,
                val fertilizer: String,
                override val location: CellsSelection? = null
            ) : CellCommand()
    
            data class Mulch(
                override val bedId: UUID,
                val started: Date,
                val volume: Double,
                val material: String,
                override val location: CellsSelection? = null
            ) : CellCommand()
    
            data class Water(
                override val bedId: UUID,
                val started: Date,
                val volume: Double,
                override val location: CellsSelection? = null
            ) : CellCommand()
    
            data class Harvest(
                override val bedId: UUID,
                val started: Date,
                val plantType: String,
                val plantCultivar: String,
                val quantity: Int? = null,
                val weight: Double? = null,
                override val location: CellsSelection? = null
            ) : CellCommand()
        }
    }
    
    Note: that the "location" property is special. It can be sent in JSON as
    a String, because there is a special deserializer. But, you need to
    understand the variations. Try to translate spoken language that matches 
    any of the of following general types of location statements into the 
    "location" property:
    
    * A1 -- this is a spreasheet-like address of a single cell. You can encode 
    as simply "A1"
    * 1 1 -- same as above, encode as "1,1"
    * two two -- Also same intent as above, but please do your best to 
    translate these written-out words to their numeric values: "2,2"
    * 3,2 -- Same intent as above, but with an actual comma! Translate to "3,2"
    * A1 to B3 -- this is a "range" location, and it has several forms, including:
        * A1 , B3 -> "1,1 to 2,3"
        * 1 1 - 2 3 "1,1 to 2,3" 
        * one one to two three -> "1,1 to 2,3"
        * four five until five six -> "4,5 to 5,6"

So, interpret all following messages in light of what I just 
wrote above.

"""

@RestController
class NlpCommandHandler(
    chatClientBuilder: ChatClient.Builder,
    private val mapper: ObjectMapper,
    private val sseEventBus: SseEventBus,
    private val applicationScope: CoroutineScope
) {
    private val client: ChatClient =
        chatClientBuilder
            .defaultAdvisors(
                SimpleLoggerAdvisor()
            )
            .build()

    @PostMapping("/api/beds/{bedId}/action")
    suspend fun action(
        @PathVariable bedId: UUID,
        @RequestParam("prompt") prompt: String
    ): Flux<String> {
        val systemMessage = SystemMessage(getSystemPrompt(bedId))
        println("SystemPrompt:")
        println(systemMessage)
        val userMessage = UserMessage(prompt)
        val response = client.prompt()
//            .options(
//                FunctionCallingOptionsBuilder().withProxyToolCalls(true).build()
//            )
            .functions("prepareBed")
            .messages(systemMessage, userMessage)
            .call()
            .chatResponse()

        val args = response!!.result.output.toolCalls[0].arguments

        val tree = mapper.readTree(args)
        val pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree)
        return Flux.just("Does this look correct to you?\n\n$pretty")
    }

    private suspend fun plantSeedlingHandler(command: PlantSeedling) {
        val bedId = command.bedId
        val bed = BedRepository.getBed(bedId)
        val bedEventBus = BedEventBus(sseEventBus, applicationScope)
        bed?.execute(command, bedEventBus)
    }

    @PostMapping("/api/beds/{bedId}/ai/plant")
    suspend fun plantSeedling(
        @PathVariable bedId: UUID,
        @RequestParam("prompt") prompt: String
    ): String {
        logNow("plantSeedling controller start", true)
        val systemMessage = SystemMessage(getSystemPrompt(bedId))
        println("SystemPrompt:")
        println(systemMessage)
        val userMessage = UserMessage(prompt)

        with (JsonArrayItemStreamer()) {
            applicationScope.launch {
                client.prompt()
                    .messages(systemMessage, userMessage)
                    .stream()
                    .content()
                    .asFlow()
                    .extractArrayItems("$.commands")
                    .collect { command ->
                        launch {
                            logNow("Found complete json object: $command")
                            val plantSeedling = mapper.readValue(command, PlantSeedling::class.java)
                            logNow("Processing command: $plantSeedling")
                            plantSeedlingHandler(plantSeedling)
                        }
                    }
            }
        }

        return "Received your request and processing it now..."
    }
}