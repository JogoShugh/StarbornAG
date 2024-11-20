package org.starbornag.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class MessagesBuilder {
    private val jacksonMapper = jacksonObjectMapper()
    private val messages = mutableListOf<Map<String, String>>()

    fun system(content: String) {
        messages.add(mapOf("role" to "system", "content" to content))
    }

    fun user(content: String) {
        messages.add(mapOf("role" to "user", "content" to content))
    }

    fun assistant(content: String) {
        messages.add(mapOf("role" to "assistant", "content" to content))
    }

    fun location(content: String) = """
        {
            "location": "$content"
        }
    """.trimIndent()

    fun build(): String {
        val map = mapOf("messages" to messages)
        return jacksonMapper.writeValueAsString(map)
    }
}

class ExamplesBuilder {
    private val examples = mutableListOf<String>()
    private var systemMessage: String = ""

    fun system(content: String) {
        systemMessage = content
    }

    fun example(block: MessagesBuilder.() -> Unit) {
        val messageBuilder = MessagesBuilder()
        messageBuilder.system(systemMessage)
        messageBuilder.block()
        examples.add(messageBuilder.build())
    }

    fun build(): List<String> {
        return examples
    }
}

fun examples(block: ExamplesBuilder.() -> Unit): List<String> {
    val examplesBuilder = ExamplesBuilder()
    examplesBuilder.block()
    return examplesBuilder.build()
}

fun main(args: Array<String>) {
    val examplesList = examples {
        system("""
            You need to translate spoken phrases indicating locations of one or more garden bed cells into a JSON structure.
            
            The JSON structure will correspond to this class:
            
            class CellsSelection(
                val row: Int? = null,
                val rows: List<Int>? = null,
                val column: Int? = null,
                val columns: List<Int>? = null,
                val cells: CellPositionList? = null,
                val cellRange: CellRange? = null,
                val cellStart: CellPosition? = null,
                val cellEnd: CellPosition? = null
            )
            
            However, the structure you need to produce is like this:
            
            {
              "location":  {...}
            }
            
            Where the {...} indicates the CellsSelection class.
            
            But there are special considerations! The API backend that will process this 
            already knows how to parse certain "location" properties if they arrive as 
            simple strings, rather than as an object with one or more of the above properties!
            
            Here are examples:
            
            * "one one" -> location: "1,1" # A single cell, equivalent to cell: "1,1"
            * "one one to two two" -> location: "1,1 - 2,2" # A range, equivalent to cellRange: ["1,1 - 2,2"]
            * "one one two two two" -> location: "1,1 - 2,2" # A range again, but notice that "two" was used instead of "to"? 
                That's important because it reflects the reality of spoken language, but it must map like I have told you 
            * "row 1" -> location: { row: 1 } 
            * "rows 1 and 2" -> location: { rows: [1,2] }
            * "cols 1 and 2" -> location: { columns: [1, 2] }
            * "columns A and B" -> location: { columns: [1, 2] }
            * "cells one one til three three" -> location: "1,1 - 3,3"
            * "squares 1 1 to 3 3" -> location: "1,1 - 3,3"
                      
            I hope you are getting the picture, but I will provide many more examples in fine-tuning.
        """.trimIndent()
        )
        listOf(
            "one one",
            "A 1",
            "A A",
            "A one",
            "A    1",
            "1 1",
            "A:1",
            "1:1",
            "1,1",
            "one,one",
            "one: one",
            "0,0", // For zero based programmers who forget this is 1-based
            "0 :0",
            "zero zero",
            "zero:zero",
            ).forEach {
            example {
                user(it)
                assistant(location("1,1"))
            }
        }
        listOf(
            "one two two three three",
            "1:2 to 3:3",
            "one two to three three",
            "one two - three three",
            "1 2 until 3 3",
            "1 2 til 3 3",
            "1 2 through three three",
            "one two through three three",
            "a two two three three",
            "a two to three three",
            "a to two three three",
            "a to to three three",
            "A two through three three"
        ).forEach {
            example {
                user(it)
                assistant(location("1,2 - 3,3"))
            }
        }
    }

    println(examplesList.joinToString("\n"))
}
