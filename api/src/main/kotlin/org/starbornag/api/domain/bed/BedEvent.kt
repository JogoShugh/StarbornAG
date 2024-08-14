package org.starbornag.api.domain.bed

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.marlonlom.utilities.timeago.TimeAgo
import java.time.Instant
import java.util.*

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@JsonSubTypes(
//    JsonSubTypes.Type(value = BedWatered::class, name = "watered"),
//    JsonSubTypes.Type(value = BedFertilized::class, name = "fertilized"),
//    JsonSubTypes.Type(value = BedMulched::class, name = "mulched")
//)
//open class BedEvent(
//    val started : Date,
//    val ended: Date = Date.from(Instant.now())
//) {
//    val startedDescription : String
//        get() = TimeAgo.using(started.time)
//
//    val endDescription : String
//        get() = TimeAgo.using(ended.time)
//}
//
//class BedWatered(started: Date, val volume: Double) : BedEvent(started = started)
//class BedFertilized(started: Date, volume: Double, val fertilizer: String) : BedEvent(started = started)
//class BedMulched(started: Date, volume: Double, val material: String) : BedEvent(started = started)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type") // Keep for subtype serialization
sealed class BedEvent(
    val bedId: UUID,
    val bedCellId: UUID,
    val started : Date,
    val ended: Date = Date.from(Instant.now())
) {
    val startedDescription : String
        get() = TimeAgo.using(started.time)

    val endDescription : String
        get() = TimeAgo.using(ended.time)
}

@JsonTypeName("planted") // Add subtype names for each subclass
class BedCellPlanted(
    bedId: UUID,
    bedCellId: UUID,
    started: Date,
    val plantType: String,
    val plantCultivar: String
) : BedEvent(bedId, bedCellId, started)

@JsonTypeName("watered") // Add subtype names for each subclass
class BedCellWatered(
    bedId: UUID,
    bedCellId: UUID,
    started: Date,
    val volume: Double
) : BedEvent(bedId, bedCellId, started)

@JsonTypeName("fertilized")
class BedFertilized(
    bedId: UUID,
    bedCellId: UUID,
    started: Date,
    val volume: Double,
    val fertilizer: String
) : BedEvent(bedId, bedCellId, started)

@JsonTypeName("mulched")
class BedMulched(
    bedId: UUID,
    bedCellId: UUID,
    started: Date,
    val volume: Double,
    val material: String
) : BedEvent(bedId, bedCellId, started)

@JsonTypeName("bedHarvested")
class BedHarvested(
    bedId: UUID,
    bedCellId: UUID,
    started: Date,
    val plantType: String,
    val plantCultivar: String,
    val quantity: Int? = null,
    val weight: Double? = null
) : BedEvent(bedId, bedCellId, started)
