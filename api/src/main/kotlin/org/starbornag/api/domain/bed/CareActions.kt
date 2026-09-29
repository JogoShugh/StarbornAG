package org.starbornag.api.domain.bed

/** The care a gardener can give the cells of a bed. */
enum class CareAction { PLANT, WATER, FERTILIZE, MULCH, HARVEST }

/** What is possible across a bed's cells right now, and which plant types can be harvested. */
data class PossibleCare(val actions: Set<CareAction>, val harvestable: Set<String>)

/**
 * The care possible across [cells], by the same soil rules [BedCell.targets] enforces: planting
 * while some cell is empty, and a harvest once something grows, for the plant types that grow.
 * Affordances are built from this, so what is offered and what is accepted cannot drift apart.
 */
fun possibleCare(cells: List<BedCell>): PossibleCare {
    val harvestable = cells.mapNotNull { it.currentPlantType }.toSortedSet()
    val actions = buildSet {
        if (cells.any { !it.isPlanted }) add(CareAction.PLANT)
        if (cells.isNotEmpty()) addAll(listOf(CareAction.WATER, CareAction.FERTILIZE, CareAction.MULCH))
        if (harvestable.isNotEmpty()) add(CareAction.HARVEST)
    }
    return PossibleCare(actions, harvestable)
}
