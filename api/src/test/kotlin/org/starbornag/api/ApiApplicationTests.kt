package org.starbornag.api.domain.bed

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.internal.JsonFormatter.prettyPrint
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.starbornag.api.domain.bed.BedCommand.*
import org.starbornag.api.rest.bed.BedCommandHandler
import org.starbornag.api.rest.bed.BedCommandMapper
import org.starbornag.api.rest.bed.BedHistoryQueryHandler
import org.starbornag.api.rest.bed.BedResourceWithCurrentState
import org.starbornag.api.rest.bed.BedResourceWithHistory
import org.starbornag.api.rest.bed.PrepareBedCommandHandler
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@WebFluxTest(controllers = [
	PrepareBedCommandHandler::class,
	BedCommandHandler::class,
	BedHistoryQueryHandler::class
])
@Import(BedCommandMapper::class)
@AutoConfigureRestDocs("build/generated-snippets")
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiApplicationTests(
	private val objectMapper: ObjectMapper,
	private val webTestClient: WebTestClient,
) {
	private var bedUuid : UUID = UUID.randomUUID()

	@Test
	fun `PrepareBedCommand for 8 by 4 bed returns BedResource with 32 empty cells and 8 columns and 4 rows`() {
		val rowCount = 4
		val cellPerRowCount = 8
		val command = PrepareBedCommand(
			UUID.randomUUID(),
			"Earth",
			Dimensions(cellPerRowCount, rowCount)
		)

		webTestClient.post()
			.uri("/api/beds")
			.bodyValue(command)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(BedResourceWithCurrentState::class.java)
			.consumeWith(document("prepare-bed"))
			.value {
				printResponse(it)
				val resource = it as BedResourceWithCurrentState
				val plantLink = resource.link("plant")
				val waterLink = resource.link("water")
				val fertilizeLink = resource.link("fertilize")
				val historyLink = resource.link("history")

				bedUuid = resource.id
				assertThat(resource.rows.count()).isEqualTo(rowCount)
				assertThat(resource.rows.all {
					assertThat(it.cells.count()).isEqualTo(cellPerRowCount)
					it.cells.all {
						it == ""
					}
				})
				assertThat(resource.name).isEqualTo("Earth")

				postCommand(plantLink, "plant-seedling",
					PlantSeedlingInBedCommand(
					bedUuid,
					1,
					1,
					"Tomato",
					"Dark Galaxy"
					)
				)

				mapOf(
					1 to Date.from(Instant.now().minusSeconds(30L)),
					2 to Date.from(Instant.now().minus(2L, ChronoUnit.DAYS)),
					3 to Date.from(Instant.now().minus(300L, ChronoUnit.HOURS))).forEach {
					val command = WaterBedCommand(
						bedUuid,
						it.value,
						2.0
					)
					postCommand(waterLink, "water-bed", command)
				}

				postCommand(fertilizeLink, "fertilize-bed", FertilizeBedCommand(
					bedUuid,
					Date.from(Instant.now().minus(2L, ChronoUnit.HOURS)),
					1.0,
					"Vegan Mix 3-2-2"
					)
				)

				webTestClient.get()
					.uri(historyLink)
					.exchange()
					.expectStatus().isOk()
					.expectBody(BedResourceWithHistory::class.java)
					.consumeWith(document("history"))
					.value {
						printResponse(it)
					}
			}
	}

	private fun postCommand(fertilizeLink: URI, exampleName: String, command: BedCommand) {
		webTestClient.post()
			.uri(fertilizeLink)
			.bodyValue(command)
			.exchange()
			.expectStatus().isOk()
			.expectBody(BedResourceWithCurrentState::class.java)
			.consumeWith(document("fertilize-bed"))
			.value {
				printResponse(it)
			}
	}

	private fun printResponse(it: Any?) {
		print(prettyPrint(objectMapper.writeValueAsString(it)))
	}

}

private fun BedResourceWithCurrentState.link(linkRel: String) : URI =
	this.getLink(linkRel).get().toUri()
