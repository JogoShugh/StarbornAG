package org.starbornag.api

import org.starbornag.api.domain.bed.BedCellPlanted
import org.starbornag.api.domain.bed.BedEvent

class BedEventHtmlFormatter {
    companion object {
        private val iconMap = mapOf(
            "BedCellWatered" to "💧",
            "BedFertilized" to "🌿",
            "BedMulched" to "🪵"
        )

        private fun plantTypeToIcon(plantType: String) =
            when (plantType.lowercase()) {
                "tomato" -> "🍅"
                "eggplant" -> "🍆"
                "potato" -> "🥔"
                "carrot" -> "🥕"
                "corn" -> "🌽" // or "ear of corn"
                "hot pepper" -> "🌶️"
                "bell pepper" -> "🫑"
                "cucumber" -> "🥒"
                "broccoli" -> "🥦"
                "garlic" -> "🧄"
                "onion" -> "🧅"
                "lettuce" -> "🥬"
                "sweet potato" -> "🍠"
                "chili pepper" -> "🌶"
                "mushroom" -> "🍄"
                "peanuts" -> "🥜"
                "beans" -> "🫘"
                "chestnut" -> "🌰"
                "ginger root" -> "🫚"
                "shallot" -> "🫛"
                "herb" -> "🌿" // If you want to include herbs
                else -> "" // Default case (for unknown plant types)
            }

        fun convert(event: BedEvent): String =
            when (event) {
                is BedCellPlanted -> plantTypeToIcon(event.plantType)
                else -> {
                    val className = event.javaClass.simpleName
                    iconMap[className] ?: ""
                }
            }
    }
}