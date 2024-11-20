package org.starbornag.api

import ch.rasc.sse.eventbus.config.SseEventBusConfigurer
import org.springframework.context.annotation.Configuration

@Configuration
class SseEventBusConfiguration : SseEventBusConfigurer {
    override fun bypassDataObjectConverters(): Boolean = true
}