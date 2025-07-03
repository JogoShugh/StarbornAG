package org.starbornag.api.rest.bed

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.html.*
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.starbornag.api.domain.bed.BedCellRepository
import org.starbornag.api.domain.bed.BedRepository
import org.starbornag.api.domain.bed.command.CellPosition
import org.starbornag.api.domain.bed.command.hasDirection
import java.util.*
import kotlin.reflect.KProperty

class HxMap(private val map: Map<String, Any>) {
    override fun toString(): String = jacksonObjectMapper().writeValueAsString(this.map)
}

private fun CommonAttributeGroupFacade.getAttrString(name: String) = attributes[name] ?: ""
private fun CommonAttributeGroupFacade.getAttrBoolean(name: String) = attributes[name].toBoolean()
private fun CommonAttributeGroupFacade.getAttrMap(name: String) : HxMap {
    val json = attributes[name]
    val typeReference = object : TypeReference<Map<String, Any>>() {}
    val map: Map<String, Any> = jacksonObjectMapper().readValue(json, typeReference)
    return HxMap(map)
}
private fun CommonAttributeGroupFacade.setAttrMap(name: String, value: Map<String, Any>) {
    val json = jacksonObjectMapper().writeValueAsString(value)
    setAttr(name, json)
}

private fun <T> CommonAttributeGroupFacade.setAttr(name: String, value: T) { attributes[name] = value.toString() }

// Extension properties for hx-get and hx-target
private const val HX_GET = "hx-get"
private const val HX_TARGET = "hx-target"
private const val HX_POST = "hx-post"
private const val HX_PUT = "hx-put"
private const val HX_DELETE = "hx-delete"
private const val HX_PUSH_URL = "hx-push-url"
private const val HX_SWAP = "hx-swap"
private const val HX_TRIGGER = "hx-trigger"
private const val HX_SELECT = "hx-select"
private const val HX_BOOST = "hx-boost"
private const val HX_CONFIRM = "hx-confirm"
private const val HX_HEADERS = "hx-headers"
private const val HX_EXT = "hx-ext"
private const val HX_DISINHERIT = "hx-disinherit"
private const val SSE_CONNECT = "sse-connect"
private const val SSE_SWAP = "sse-swap"

val iconMap = mapOf(
    "BedCellWatered" to "💧",
    "BedFertilized" to "🌿",
    "BedMulched" to "🪵"
)

fun plantTypeToIcon(plantType: String) =
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

class AttrDelegate(private val consumer: CommonAttributeGroupFacade,
                   private val attrName: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        consumer.getAttrString(attrName)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
        consumer.setAttr(attrName, value)
}

class AttrDelegateBoolean(private val consumer: CommonAttributeGroupFacade,
                   private val attrName: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        consumer.getAttrBoolean(attrName)

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
        consumer.setAttr(attrName, value)
}

class HX(private val consumer: CommonAttributeGroupFacade) {
    var HX.get: String by AttrDelegate(consumer, HX_GET)
    var HX.post: String by AttrDelegate(consumer, HX_POST)
    var HX.put: String by AttrDelegate(consumer, HX_PUT)
    var HX.trigger : String by AttrDelegate(consumer, HX_TRIGGER)
    var HX.delete: String by AttrDelegate(consumer, HX_DELETE)
    var HX.target: String by AttrDelegate(consumer, HX_TARGET)
    var HX.pushUrl: Boolean by AttrDelegateBoolean(consumer, HX_PUSH_URL)
    var HX.disinherit: String by AttrDelegate(consumer, HX_DISINHERIT)
    var HX.ext: String by AttrDelegate(consumer, HX_EXT)
    var HX.sseConnect: String by AttrDelegate(consumer, SSE_CONNECT)
    var HX.sseSwap: String by AttrDelegate(consumer, SSE_SWAP)
    var HX.swap: String by AttrDelegate(consumer, HX_SWAP)
    operator fun invoke(block: HX.() -> Unit) = block()
}

val CommonAttributeGroupFacade.hx: HX
    get() = HX(this)

@RestController
class IndexController {
    @GetMapping("/beds/{bedId}")
    suspend fun index(@PathVariable bedId: UUID): ResponseEntity<String> {
        val bed = BedRepository.getBed(bedId)
        val bedResource = BedResourceWithCurrentState.from(bed!!)
        val cellsPerRow = bedResource.rows[0].cells.count()

        val html = createHTML().html {
                head {
                    title { +bedResource.name }
                    script(src = "https://unpkg.com/htmx.org@2.0.2") {}
                    script(src = "https://unpkg.com/htmx-ext-sse@2.2.2/sse.js") {}
                    script(src= "//cdnjs.cloudflare.com/ajax/libs/annyang/2.6.1/annyang.min.js") {}
                    script(src = "//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/speechkitt.min.js") {}
                    script {
                        unsafe {
                            +"""                                                  
function starbornSend(entry) {                                       
      fetch('/api/beds/$bedId/ai/plant', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: "prompt=" + entry 
      })
      .then(response => { 
        if (response.ok) {
          console.log("Sent entry: " + entry); 
        } else {
          console.error('Error:', response.status); 
        }
      })
      .catch(error => {
        console.error('Error:', error);
      });
}

    document.addEventListener('DOMContentLoaded', function () {
if (annyang) {
  annyang.addCommands({
    'starborn *entry': function(entry) { starbornSend(entry); }
  });
  SpeechKITT.annyang();
  SpeechKITT.setInstructionsText("Example: Starborn, watered row 1.")
  SpeechKITT.setStylesheet('//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/themes/flat-pumpkin.css');
  SpeechKITT.vroom();
}


//  // Check for browser support
//  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
//  if (!SpeechRecognition) {
//    console.error("Speech Recognition API is not supported in this browser.");
//    return;
//  }
//
//  const recognition = new SpeechRecognition();
//  recognition.continuous = true; // Keep listening continuously
//  recognition.interimResults = true; // Enable interim results
//
//  let listeningForStarborn = true; // Start by listening for the keyword
//  let capturedText = ""; // Accumulated text after "Starborn"
//  let interimText = ""; // Buffer for interim results
//  let lastUpdateTime = Date.now(); // Track the last time we got results
//  const SILENCE_THRESHOLD = 750; // 0.75 seconds of silence
//
//  // Function to log a captured sentence
//  const logCapturedSentence = () => {
//    if (capturedText.trim()) {
//      console.log("Captured:", capturedText.trim());
//      capturedText = ""; // Reset for the next sentence
//      listeningForStarborn = true; // Reset to listen for "Starborn"
//    }
//  };
//
//  // Silence monitoring using setInterval
//  const silenceMonitorInterval = setInterval(() => {
//    const now = Date.now();
//    if (!listeningForStarborn && now - lastUpdateTime >= SILENCE_THRESHOLD) {
//      logCapturedSentence();
//    }
//  }, 100); // Check every 100ms
//
//  // Handle speech recognition results
//  recognition.onresult = (event) => {
//    for (let i = event.resultIndex; i < event.results.length; i++) {
//      const result = event.results[i];
//      const transcript = result[0].transcript.trim().toLowerCase();
//      const isFinal = result.isFinal;
//
//      if (listeningForStarborn) {
//        // Look for the keyword "Listen"
//        // console.log("Heard:", transcript);
//        if (transcript.includes("listen") && isFinal) {
//          console.log("Listen detected!");
//          listeningForStarborn = false; // Start capturing speech
//          capturedText = ""; // Reset captured text
//          interimText = ""; // Clear interim buffer
//        }
//      } else {
//        // Accumulate text after "Starborn"
//        if (isFinal) {
//          capturedText += interimText + " "; // Add finalized interim text
//          interimText = ""; // Clear interim buffer
//        } else {
//          interimText = transcript; // Update interim text
//        }
//        lastUpdateTime = Date.now(); // Update the last result time
//      }
//    }
//  };
//
//  // Handle errors and attempt to recover
//  recognition.onerror = (event) => {
//    console.error("Speech recognition error:", event.error);
//    if (event.error === "no-speech" || event.error === "audio-capture") {
//      console.log("Restarting recognition...");
//      recognition.stop();
//      recognition.start();
//    }
//  };
//
//  // Start recognition
//  recognition.start();
                                                      
                                // Manual input                       
//                                document.getElementById('voiceInputButton').addEventListener('click', function() {
//                                    var transcribedText = document.getElementById('voiceInputText').value;
//                                    console.log(transcribedText);
//                                  fetch('/api/beds/$bedId/ai/plant', {
//                                    method: 'POST',
//                                    headers: {
//                                      'Content-Type': 'application/x-www-form-urlencoded',
//                                    },
//                                    body: "prompt=" + transcribedText 
//                                  })
//                                  .then(response => { 
//                                    if (response.ok) {
//                                      document.getElementById('voiceInputText').value = ""; 
//                                    } else {
//                                      console.error('Error:', response.status); 
//                                    }
//                                  })
//                                  .catch(error => {
//                                    console.error('Error:', error);
//                                  });
//                                });
                            });
                            """
                        }
                    }

                    //script(src = "/htmx.js") {}
                    //script(src = "/sse.js") {}
                    script {
                        type = "text/javascript"
                        src = "/script.js"
                    }
                    link {
                        rel = "stylesheet"
                        href = "/styles.css"
                    }
                }
                body {
                    id = "garden"
                    classes = setOf("sample-transition")
                    div {
                        classes = setOf("centered-container")
                        h1 {
                            classes = setOf("garden-name")
                            +bedResource.name
                        }

                        // The Voice Input Button and Input Box (combined)
//                        section {
//                            id = "voiceInputContainer"
//
//                            textArea {
//                                id = "voiceInputText"
//                                attributes["x-webkit-speech"] = ""
//                            }
//
//                            div {
//                                button {
//                                    id = "voiceInputButton"
//                                    classes = setOf("btn", "btn-primary")
//                                    style = "margin: 8px; background-color: goldenrod;"
//
//                                    +" 🎤 Dictate Activity Log"
//                                }
//                            }
//
//                        } // voiceInputContainer

                        div {
                            id = "bed-$bedId"
                            classes = setOf("grid-container")
                            hx {
                                ext = "sse"
                                sseConnect = "/api/beds/$bedId/events?clientId=${UUID.randomUUID()}"
                            }
                            style {
                                +"""
                                    .grid-container {
                                        grid-template-columns: repeat($cellsPerRow, 1fr);
                                    }
                                    """.trimIndent()
                            }
                            bedResource.rows.forEach { row ->
                                row.cells.forEach { cell ->
                                    div {
                                        classes = setOf("grid-item")
                                        hx {
                                            get = "/beds/$bedId/detail/${cell.bedCellId}"
                                            target = "#bed-$bedId"
                                            swap = "outerHTML transition:true"
                                            pushUrl = true
                                        }
                                        val plantIcon = plantTypeToIcon(cell.planting.plantType)

                                        div {
                                            classes = setOf("plant")
                                            span {
                                                hx {
                                                    sseSwap = "plants-" + cell.bedCellId.toString()
                                                    target = "this"
                                                }
                                                classes = setOf(
                                                    "plant-icon",
                                                    "large-icon"
                                                )
                                                +plantIcon
                                            }
                                        }

                                        div {
                                            classes = setOf("events")
                                            hx {
                                                sseSwap = "events-" + cell.bedCellId.toString()
                                                swap = "beforeend"
                                                target = "this"
                                            }
                                            cell.events.forEach {
                                                val type = it.javaClass
                                                span {
                                                    +iconMap.getOrDefault(type.simpleName, "")
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

            return ResponseEntity.ok(html)
        }

        @GetMapping("/beds/{bedId}/detail/{bedCellId}")
        suspend fun detail(@PathVariable bedId: UUID, @PathVariable bedCellId: UUID): ResponseEntity<String> {

            val bedCell = BedCellRepository.getBedCell(bedCellId)

            val location = bedCell.cellPosition
            val parentBedDimensions = bedCell.parentBedDimension

            val neighbors = location.neighbors(parentBedDimensions.rows, parentBedDimensions.columns)

            val fragment = buildString {
                appendHTML().div {
                    classes = setOf("centered-container")
                    style {
                        +"""
                       .grid-container {
                            grid-template-columns: repeat(3, 1fr);
                            grid-template-rows: repeat(3, 1fr);
                       }
                       """.trimIndent()
                    }
                    div {
                        classes = setOf("grid-container")
                        buildDirectionCells(neighbors, "nw", "n", "ne", "w")
                        div {
                            classes = setOf("grid-solo")
                            +("Location:" + location.row.toString() + ":" + location.column.toString())
                            +(neighbors.joinToString(","))
                            +(bedCell.planting.plantType + " : " + bedCell.planting.plantCultivar)
                            ul {
                                bedCell.events.forEach {
                                    li {
                                        +(it.ended.toInstant().toString() + " -- " + it.javaClass.simpleName)
                                    }
                                }
                            }
                        }
                        buildDirectionCells(neighbors, "e", "sw", "s", "se")
                    }
                }
            }
            return ResponseEntity.ok(fragment)
        }
    }

    fun DIV.buildDirectionCells(neighbors: List<Pair<String, CellPosition>>, vararg directions: String) {
        directions.forEach { direction ->
            if (neighbors.hasDirection(direction)) {
                div {
                    classes = setOf("arrow", direction)
                }
            } else {
                div {
                    classes = setOf("arrow")
                }
            }
        }
    }

