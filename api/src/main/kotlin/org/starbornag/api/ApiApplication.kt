package org.starbornag.api

import ch.rasc.sse.eventbus.config.EnableSseEventBus
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

/*

$.commands[*] -> fire event for each item within this matching context

this means:

check for START_OBJECT, FIELD_NAME matching "commands", then Start Array

at this point, we must begin checking for pairs of balanced start and end object or start and end array (nested array),
or simply value events

If we enter into a Start object, we only emit after we get back down to N end Object observations
Likewhere, if we entter into Start Array, we only emit after balancing back down to 0

if the first few events don't match start_object field_name start_array, then we will never match, so exit



 */

@SpringBootApplication(scanBasePackages = ["org.starbornag.api", "org.starbornag.api.rest.bed"])
@EnableSseEventBus
class ApiApplication
fun main(args: Array<String>) {

//	val orig = """
//		{
//		 	"commands": [
//		 		{ "com": "Run" },
//		 		{ "nested": { "prop": "obj" } },
//		 		true,
//		 		false,
//		 		null,
//		 		"Stringy!",
//		 		[ [1,2], [3,4] ],
//		 		{ "com": "Jump", "howHigh": 2.0 }
//		 	]
//		}
//	""".trimIndent()
//
//	println("The json:")
//	println(orig)
//
//	val json = orig.toByteArray(StandardCharsets.UTF_8)
//	val parser = JsonParser(StandardCharsets.UTF_8)
//	var pos = 0
//	var event = 0
//
//	val extractor = JsonValueExtractor("$.commands")
//
//	while (true) {
//		// Feed the parser until it returns a new event
//		while (parser.nextEvent().also { event = it } == JsonEvent.NEED_MORE_INPUT) {
//			pos += parser.feeder.feed(json, pos, json.count() - pos)
//
//			// Indicate end of input to the parser
//			if (pos == json.count()) {
//				parser.feeder.done()
//			}
//		}
//
//		// Handle event
//		extractor.processEvent(event, parser.currentString)
//		if (event == JsonEvent.ERROR) {
//			throw IllegalStateException("Syntax error in JSON text")
//		}
//
//		if (event == JsonEvent.EOF) {
//			break
//		}
//
//	}
	val config = runApplication<ApiApplication>(*args)
}
