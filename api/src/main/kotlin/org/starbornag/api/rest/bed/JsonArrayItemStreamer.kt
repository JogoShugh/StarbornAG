package org.starbornag.api.rest.bed

import de.undercouch.actson.JsonEvent
import de.undercouch.actson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import org.starbornag.api.LogTimer.logNow
import java.nio.charset.StandardCharsets

class JsonArrayItemStreamer() {
    enum class ArrayItemContext(val startEvent: Int, val endEvent: Int) {
        ARRAY(JsonEvent.START_ARRAY, JsonEvent.END_ARRAY),
        OBJECT(JsonEvent.START_OBJECT, JsonEvent.END_OBJECT),
        VALUE(0, 0)  // No start/end events for VALUE
    }

    private var targetArrayName = ""
    private var foundFieldName = ""
    private var depth = 0
    private var awaitingTargetArray = true
    private var arrayItemContext = ArrayItemContext.VALUE
    private val currentValue = StringBuilder()
    private val parser = JsonParser(StandardCharsets.UTF_8)
    private val events = mutableListOf<Int>()
    private var event = 0

    private fun getJsonFragment(event: Int, value: String = ""): String {
        return when (event) {
            JsonEvent.START_OBJECT -> "{"
            JsonEvent.END_OBJECT -> "}"
            JsonEvent.START_ARRAY -> "["
            JsonEvent.END_ARRAY -> "]"
            JsonEvent.FIELD_NAME -> "\"$value\":"
            JsonEvent.VALUE_STRING -> "\"$value\","
            JsonEvent.VALUE_INT, JsonEvent.VALUE_DOUBLE -> "$value,"
            JsonEvent.VALUE_TRUE -> "true,"
            JsonEvent.VALUE_FALSE -> "false,"
            JsonEvent.VALUE_NULL -> "null,"
            else -> ""
        }
    }

    private fun extractArrayName(jsonPath: String): String {
        if (jsonPath.startsWith("$.") && jsonPath.length > 2) {
            return jsonPath.substring(2)
        } else {
            throw IllegalArgumentException("Invalid JSON path format. Use '$.arrayName'")
        }
    }

    private fun stripTrailingComma(value: String): String {
        return value.dropLastWhile { it == ',' }
    }

    private fun formatCurrentValue(currentValue: StringBuilder): String {
        val fixed = currentValue.replace(Regex(",}"), "}").replace(Regex(",]"), "]")
        return fixed
    }

    private fun isScalarValueOrField(event: Int): Boolean {
        return listOf(
            JsonEvent.FIELD_NAME,
            JsonEvent.VALUE_STRING,
            JsonEvent.VALUE_INT,
            JsonEvent.VALUE_DOUBLE,
            JsonEvent.VALUE_TRUE,
            JsonEvent.VALUE_FALSE,
            JsonEvent.VALUE_NULL
        ).contains(event)
    }

    private fun readyForArrayItemEmission() = (events.count() == 2
            && events[0] == JsonEvent.START_OBJECT
            && events[1] == JsonEvent.FIELD_NAME
            && foundFieldName == targetArrayName)

    private fun updateCurrentValue(event: Int, currentString: String = "") =
        currentValue.append(getJsonFragment(event, currentString))

    private fun setContext(context: ArrayItemContext) {
        arrayItemContext = context
        depth = 1
    }

    private fun setContextByEvent(event: Int) {
        when (event) {
            JsonEvent.START_OBJECT -> setContext(ArrayItemContext.OBJECT)
            JsonEvent.START_ARRAY -> setContext(ArrayItemContext.ARRAY)
        }
    }

    private fun resetContext() {
        arrayItemContext = ArrayItemContext.VALUE
        events.clear()
        depth = 0
        currentValue.clear()
    }

    suspend fun Flow<String>.extractArrayItems(sourceArrayPath: String = "$.items"): Flow<String> = flow {
        targetArrayName = extractArrayName(sourceArrayPath)

        onCompletion {
            parser.feeder.done()
        }

        collect { fragment ->
            //logNow("Fragment = $fragment")
            suspend fun handleCompoundArrayItemContext(context: ArrayItemContext, event: Int, currentString: String) {
                when (isScalarValueOrField(event)) {
                    true -> updateCurrentValue(event, currentString)
                    false -> {
                        updateCurrentValue(event)
                        when (event) {
                            context.startEvent -> depth++
                            context.endEvent -> {
                                depth--
                                if (depth == 0) {
                                    emit(formatCurrentValue(currentValue))
                                    resetContext()
                                }
                            }
                        }
                    }
                }
            }

            val bytes = fragment.toByteArray()
            var position = 0
            while (position < bytes.count()) {
                position += parser.feeder.feed(bytes, position, bytes.count() - position)
                val nextEvent = parser.nextEvent()
                if (nextEvent != JsonEvent.NEED_MORE_INPUT) {
                    event = nextEvent
                    break
                } else {
                    return@collect
                }
            }

            if (event == JsonEvent.ERROR) {
                throw IllegalStateException("Syntax error in JSON text")
            }

            val currentString = parser.currentString

            when (awaitingTargetArray) {
                true -> when (event) {
                    JsonEvent.START_OBJECT -> {
                        events.add(event)
                    }

                    JsonEvent.FIELD_NAME -> {
                        events.add(event)
                        foundFieldName = currentString
                    }

                    JsonEvent.START_ARRAY -> {
                        if (readyForArrayItemEmission()) {
                            awaitingTargetArray = false
                            resetContext()
                        }
                    }
                }

                false -> {
                    when (arrayItemContext) {
                        ArrayItemContext.VALUE -> {
                            when (isScalarValueOrField(event)) {
                                true -> {
                                    val value = stripTrailingComma(getJsonFragment(event, currentString))
                                    emit(value)
                                }

                                false -> {
                                    when (event) {
                                        JsonEvent.START_OBJECT, JsonEvent.START_ARRAY -> {
                                            updateCurrentValue(event)
                                            setContextByEvent(event)
                                        }
                                    }
                                }
                            }
                        }

                        ArrayItemContext.OBJECT, ArrayItemContext.ARRAY -> {
                            when (isScalarValueOrField(event)) {
                                true -> updateCurrentValue(event, currentString)
                                false -> {
                                    updateCurrentValue(event)
                                    when (event) {
                                        arrayItemContext.startEvent -> depth++
                                        arrayItemContext.endEvent -> {
                                            depth--
                                            if (depth == 0) {
                                                emit(formatCurrentValue(currentValue))
                                                resetContext()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}