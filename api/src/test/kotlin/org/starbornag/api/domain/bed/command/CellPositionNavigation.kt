package org.starbornag.api.domain.bed.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val rows = 4
private const val cols = 8

class CellPositionNavigation {
    @Test
    fun `it has the correct neighbors for all cells`() {
        val testCases = listOf(
            CellPosition(1, 1) to (3 to setOf("e", "se", "s")),
            CellPosition(1, 2) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 3) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 4) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 5) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 6) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 7) to (5 to setOf("w", "e", "sw", "s", "se")),
            CellPosition(1, 8) to (3 to setOf("w", "sw", "s")),

            CellPosition(2, 1) to (5 to setOf("n", "ne", "e", "se", "s")),
            CellPosition(2, 2) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 3) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 4) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 5) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 6) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 7) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(2, 8) to (5 to setOf("nw", "n", "w", "sw", "s")),

            CellPosition(3, 1) to (5 to setOf("n", "ne", "e", "se", "s")),
            CellPosition(3, 2) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 3) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 4) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 5) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 6) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 7) to (8 to setOf("nw", "n", "ne", "w", "e", "sw", "s", "se")),
            CellPosition(3, 8) to (5 to setOf("nw", "n", "w", "sw", "s")),

            CellPosition(4, 1) to (3 to setOf("n", "ne", "e")),
            CellPosition(4, 2) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 3) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 4) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 5) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 6) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 7) to (5 to setOf("nw", "n", "ne", "w", "e")),
            CellPosition(4, 8) to (3 to setOf("nw", "n", "w"))
        )

        for ((position, expected) in testCases) {
            val (expectedNeighborCount, expectedDirections) = expected
            val neighbors = position.neighbors(rows, cols)

            assertThat(neighbors.size)
                .`as`("Neighbor count for $position")
                .isEqualTo(expectedNeighborCount)

            val directions = neighbors.map { it.first }.toSet()
            assertThat(directions)
                .`as`("Directions for $position")
                .isEqualTo(expectedDirections)
        }
    }

}