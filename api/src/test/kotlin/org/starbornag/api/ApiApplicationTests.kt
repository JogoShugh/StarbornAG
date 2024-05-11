package org.starbornag.api.domain.bed

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.internal.JsonFormatter.prettyPrint
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
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

@WebFluxTest(
    controllers = [
        PrepareBedCommandHandler::class,
        BedCommandHandler::class,
        BedHistoryQueryHandler::class
    ]
)
@Import(BedCommandMapper::class)
@AutoConfigureRestDocs("build/generated-snippets")
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiApplicationTests(
    private val objectMapper: ObjectMapper,
    private val webTestClient: WebTestClient,
) {
    private var bedUuid: UUID = UUID.randomUUID()

    @Test
    fun `Create bed of 8 feet by 4 feet and plant, water, and fertilize it`() {
        val rowCount = 4
        val cellPerRowCount = 8
        val prepareBedCommand = PrepareBedCommand(
            UUID.randomUUID(),
            "Earth",
            Dimensions(cellPerRowCount, rowCount)
        )

        postCommand<BedResourceWithCurrentState>(
            URI.create("/api/beds"),
            "prepare-bed",
            prepareBedCommand,
            HttpStatus.CREATED
        ) {
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

            postCommand<BedResourceWithCurrentState>(
                plantLink,
                "plant-seedling",
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
                3 to Date.from(Instant.now().minus(300L, ChronoUnit.HOURS))
            ).forEach {
                val waterBedCommand = WaterBedCommand(
                    bedUuid,
                    it.value,
                    2.0
                )
                postCommand<BedResourceWithCurrentState>(
                    waterLink,
                    "water-bed",
                    waterBedCommand
                )
            }

            postCommand<BedResourceWithCurrentState>(
                fertilizeLink,
                "fertilize-bed",
                FertilizeBedCommand(
                    bedUuid,
                    Date.from(Instant.now().minus(2L, ChronoUnit.HOURS)),
                    1.0,
                    "Vegan Mix 3-2-2"
                )
            )

            getQuery<BedResourceWithHistory>(
                historyLink,
                "get-history"
            )

        }
    }

    private inline fun <reified T> getQuery(
        link: URI, exampleName: String,
        noinline valueConsumer: (Any) -> Unit = {
            printResponse(it)
        }
    ) =
        httpRequest<T>(HttpMethod.GET, link, exampleName, null, HttpStatus.OK)

    private inline fun <reified T> postCommand(
        link: URI, exampleName: String,
        command: BedCommand,
        status: HttpStatusCode = HttpStatus.OK,
        noinline valueConsumer: (Any) -> Unit = {
            printResponse(it)
        }
    ) =
        httpRequest<T>(HttpMethod.POST, link, exampleName, command, status, valueConsumer)

    private inline fun <reified T> httpRequest(
        method: HttpMethod, link: URI,
        exampleName: String,
        payload: Any? = null,
        status: HttpStatusCode,
        noinline valueConsumer: (Any) -> Unit = {
            printResponse(it)
        }
    ) {
        webTestClient.method(method)
            .uri(link).let {
                if (payload != null) it.bodyValue(payload)
                return@let it
            }
            .exchange()
            .expectStatus().isEqualTo(status)
            .expectBody(T::class.java)
            .consumeWith(document(exampleName))
            .value(valueConsumer)
    }

    private fun printResponse(it: Any?) {
        print(prettyPrint(objectMapper.writeValueAsString(it)))
    }

}

private fun BedResourceWithCurrentState.link(linkRel: String): URI =
    this.getLink(linkRel).get().toUri()
