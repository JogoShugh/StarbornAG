package org.starbornag.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.internal.JsonFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.starbornag.api.domain.bed.command.BedCommand
import org.starbornag.api.domain.bed.command.BedCommand.*
import org.starbornag.api.domain.bed.command.Dimensions
import org.starbornag.api.rest.bed.*
import java.net.URI
import java.time.Instant
import java.util.*

@WebFluxTest(
    controllers = [
        PrepareBedCommandHandler::class,
        BedCommandHandler::class,
        BedCurrentStateQueryHandler::class,
        BedHistoryQueryHandler::class,
        BedCommandSchemaQueryHandler::class
    ]
)
@Import(BedCommandMapper::class)
@AutoConfigureRestDocs("build/generated-snippets")
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiApplicationTestsCoroutines(
    private val objectMapper: ObjectMapper,
    private val webTestClient: WebTestClient,
) {
    private var bedUuid: UUID = UUID.randomUUID()

    @Test
    fun `Create bed of 8 feet by 4 feet and plant, water, and fertilize it`() {
        val bedLength = 8
        val bedHWidth = 4
        val prepareBed = BedCommand.PrepareBed(
            UUID.randomUUID(),
            "Earth",
            Dimensions(bedLength, bedHWidth),
            1
        )

        postCommand<BedResourceWithCurrentState>(
            URI.create("/api/beds"),
            "prepare-bed",
            prepareBed,
            HttpStatus.CREATED
        ) { it ->
            //printResponse(it)

            val resource = it as BedResourceWithCurrentState
            val selfLink = resource.link("self")
            val plantLink = resource.link("plant")
            val waterLink = resource.link("water")
            val fertilizeLink = resource.link("fertilize")
            val harvestLink = resource.link("harvest")
            val historyLink = resource.link("history")

            bedUuid = resource.id
            assertThat(resource.rows.count()).isEqualTo(bedHWidth)
            assertThat(resource.rows.all { row ->
                assertThat(row.cells.count()).isEqualTo(bedLength)
                row.cells.all { cell ->
                  cell.planting.isEmpty()
                }
            })
            assertThat(resource.name).isEqualTo("Earth")

//            postCommand<BedResourceWithCurrentState>(
//                plantLink,
//                "plant-seedling",
//                BedCommand.PlantSeedlingCommand(
//                    bedUuid,
//                    1,
//                    1,
//                    "Tomato",
//                    "Dark Galaxy"
//                )
//            )

            mapOf(
                1 to Date.from(Instant.now().minusSeconds(30L))
            ).forEach {
                val waterBedCommand = CellCommand.Water(
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

            getQuery<BedResourceWithCurrentState>(
                selfLink,
                "self"
            )
//
//            getQuery<BedResourceWithHistory>(
//                historyLink,
//                "get-history"
//            )
        }
    }

    private inline fun <reified T> getQuery(
        link: URI, exampleName: String,
        noinline valueConsumer: (Any?) -> Unit = {
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
                when {
                    payload != null -> it.bodyValue(payload)
                }
                return@let it
            }
            .exchange()
            .expectStatus().isEqualTo(status)
            .expectBody(T::class.java)
            .consumeWith(WebTestClientRestDocumentation.document(exampleName))
            .value(valueConsumer)
    }

    private fun printResponse(it: Any?) {
        print(JsonFormatter.prettyPrint(objectMapper.writeValueAsString(it)))
    }

}

private fun BedResourceWithCurrentState.link(linkRel: String): URI =
    this.getLink(linkRel).get().toUri()