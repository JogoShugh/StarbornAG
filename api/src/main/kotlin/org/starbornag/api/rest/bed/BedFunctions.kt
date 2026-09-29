package org.starbornag.api.rest.bed

import org.springframework.ai.tool.ToolCallback
import org.springframework.ai.tool.function.FunctionToolCallback
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
import java.util.function.Consumer

@Configuration
class BedFunctionsConfig {

    // Tools the model may call. They do nothing themselves: callers read the model's tool-call
    // arguments (see NlpCommandHandler.action) instead of letting Spring AI execute them.
    @Bean
    fun prepareBedCallback(): ToolCallback =
        FunctionToolCallback.builder("prepareBed", Consumer<PrepareBed> { })
            .description("Prepare a garden bed")
            .inputType(PrepareBed::class.java)
            .build()

    @Bean
    fun plantSeedlingCallback(): ToolCallback =
        FunctionToolCallback.builder("plantSeedling", Consumer<PlantSeedling> { })
            .description("Plant seeding in a cell")
            .inputType(PlantSeedling::class.java)
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