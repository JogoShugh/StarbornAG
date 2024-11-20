package org.starbornag.api.rest.bed

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.model.function.FunctionCallback
import org.springframework.ai.model.function.FunctionCallbackWrapper
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.reactive.ClientHttpResponseDecorator
import org.springframework.web.client.RestClient
import org.starbornag.api.domain.bed.command.BedCommand.CellCommand.PlantSeedling
import org.starbornag.api.domain.bed.command.BedCommand.PrepareBed
import org.zalando.logbook.Logbook
import java.nio.charset.StandardCharsets

@Configuration
class BedFunctionsConfig(
    val objectMapper: ObjectMapper) {

    @Bean
    fun prepareBedCallback(): FunctionCallback = FunctionCallbackWrapper.builder { _: PrepareBed -> }
        .withName("prepareBed")
        .withDescription("Prepare a garden bed")
        .withObjectMapper(objectMapper)
        .withInputType(PrepareBed::class.java)
        .build()

    @Bean
    fun plantSeedlingCallback(): FunctionCallback = FunctionCallbackWrapper.builder { _: PlantSeedling -> }
        .withName("plantSeedling")
        .withDescription("Plant seeding in a cell")
        .withObjectMapper(objectMapper)
        .withInputType(PlantSeedling::class.java)
        .build()


    class LogbookClientHttpRequestInterceptor
        : ClientHttpRequestInterceptor {
        override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution
        ): ClientHttpResponse {
            println("The request:")
            println(request.headers)
            val bodyString = String(body, StandardCharsets.UTF_8) // Convert bytes to String
            println(bodyString)
            val response = execution.execute(request, body)
            println(response)
            return response
        }
    }

    @Bean
    fun restClientCustomizer(logbook: Logbook?): RestClientCustomizer {
        return RestClientCustomizer { restClientBuilder: RestClient.Builder ->
            restClientBuilder.requestInterceptor(
                LogbookClientHttpRequestInterceptor()
            )
        }
    }
}