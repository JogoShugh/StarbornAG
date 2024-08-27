package org.starbornag.api.domain.bed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class BedCellStateRepositoryTest {
    @Test
    // TODO remove dashes from uuids in stream names
    fun `it populates BedCellAggregate from stream of events`() {
        val bedId = UUID.fromString("19690258-8d71-4484-b77c-1d8e91036b46")
        val cellId = UUID.fromString("b4d61a0a-dcdc-44cd-8e23-5e57cf4599be")

        val repo = BedCellStateRepository()
        val state = repo.fetch(bedId, cellId)

        assertThat(state).isNotNull()
    }
}