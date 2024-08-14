package org.starbornag.api.domain.bed.serialization

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DeserializationTest<T>(private val type: Class<T>) {
    abstract val baseCases : Map<String, T>

    protected fun compare(actual: T, expected: T) {
        when {
            actual is Iterable<*> && expected is Iterable<*> -> {
                assertThat(actual.count()).isEqualTo(expected.count())
                for(pair in actual.zip(expected)) {
                    assertThat(pair.first).isEqualTo(pair.second)
                }
            }
            else -> assertThat(actual).isEqualTo(expected)
        }
    }

    private fun inputs(): Stream<Arguments> =
        baseCases.entries.stream().map { (input, expected) -> Arguments.of(input, expected) }

    @ParameterizedTest
    @MethodSource("inputs")
    fun `deserializes base cases correctly`(input: String, expected: T) {
        val actual = jacksonObjectMapper().readValue(input, type)
        compare(actual, expected)
    }

    // Makes a proper json value out of the input
    protected fun jv(input: String) = "\"$input\""
}