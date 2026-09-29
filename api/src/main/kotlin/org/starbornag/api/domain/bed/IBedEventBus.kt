package org.starbornag.api.domain.bed

import org.starbornag.api.domain.bed.command.BedCommand.CellCommand

/** Port of the legacy in-memory aggregates: stores and announces a cell's event. */
interface IBedEventBus {
    suspend fun publishEvent(command: CellCommand,
                     event: BedEvent)
    suspend fun storeEvent(event: BedEvent)
}
