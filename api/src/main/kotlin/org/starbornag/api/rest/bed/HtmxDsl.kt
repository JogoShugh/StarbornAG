package org.starbornag.api.rest.bed

import kotlinx.html.CommonAttributeGroupFacade
import kotlin.reflect.KProperty

// A small typed DSL for htmx attributes on kotlinx.html elements:  div { hx { get = "/x"; target = "#y" } }

private const val HX_GET = "hx-get"
private const val HX_POST = "hx-post"
private const val HX_PUT = "hx-put"
private const val HX_DELETE = "hx-delete"
private const val HX_TARGET = "hx-target"
private const val HX_SWAP = "hx-swap"
private const val HX_SWAP_OOB = "hx-swap-oob"
private const val HX_TRIGGER = "hx-trigger"
private const val HX_PUSH_URL = "hx-push-url"
private const val HX_DISINHERIT = "hx-disinherit"
private const val HX_EXT = "hx-ext"
private const val SSE_CONNECT = "sse-connect"
private const val SSE_SWAP = "sse-swap"

class AttrDelegate(private val consumer: CommonAttributeGroupFacade, private val attrName: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = consumer.attributes[attrName] ?: ""

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        consumer.attributes[attrName] = value
    }
}

class AttrDelegateBoolean(private val consumer: CommonAttributeGroupFacade, private val attrName: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = consumer.attributes[attrName].toBoolean()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        consumer.attributes[attrName] = value.toString()
    }
}

class HX(consumer: CommonAttributeGroupFacade) {
    var get: String by AttrDelegate(consumer, HX_GET)
    var post: String by AttrDelegate(consumer, HX_POST)
    var put: String by AttrDelegate(consumer, HX_PUT)
    var delete: String by AttrDelegate(consumer, HX_DELETE)
    var target: String by AttrDelegate(consumer, HX_TARGET)
    var swap: String by AttrDelegate(consumer, HX_SWAP)
    var swapOob: Boolean by AttrDelegateBoolean(consumer, HX_SWAP_OOB)
    var trigger: String by AttrDelegate(consumer, HX_TRIGGER)
    var pushUrl: Boolean by AttrDelegateBoolean(consumer, HX_PUSH_URL)
    var disinherit: String by AttrDelegate(consumer, HX_DISINHERIT)
    var ext: String by AttrDelegate(consumer, HX_EXT)
    var sseConnect: String by AttrDelegate(consumer, SSE_CONNECT)
    var sseSwap: String by AttrDelegate(consumer, SSE_SWAP)

    operator fun invoke(block: HX.() -> Unit) = block()
}

val CommonAttributeGroupFacade.hx: HX
    get() = HX(this)

/** Care icons by event class name, as the SSE announcements append them to a cell. */
val iconMap = mapOf(
    "BedCellWatered" to "💧",
    "BedFertilized" to "🌿",
    "BedMulched" to "🪵",
    "BedHarvested" to "🧺"
)

private val plantIcons = mapOf(
    "tomato" to "🍅",
    "eggplant" to "🍆",
    "potato" to "🥔",
    "carrot" to "🥕",
    "corn" to "🌽",
    "hot pepper" to "🌶️",
    "bell pepper" to "🫑",
    "cucumber" to "🥒",
    "broccoli" to "🥦",
    "garlic" to "🧄",
    "onion" to "🧅",
    "lettuce" to "🥬",
    "sweet potato" to "🍠",
    "chili pepper" to "🌶",
    "mushroom" to "🍄",
    "peanuts" to "🥜",
    "beans" to "🫘",
    "chestnut" to "🌰",
    "ginger root" to "🫚",
    "shallot" to "🫛",
    "herb" to "🌿"
)

/** The icon for a plant type, or nothing for an empty cell or an unknown plant. */
fun plantTypeToIcon(plantType: String): String = plantIcons[plantType.lowercase()] ?: ""
