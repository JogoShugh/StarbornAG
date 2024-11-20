package org.starbornag.api

import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.stereotype.Component
import org.starbornag.api.domain.bed.BedEvent

@Component
class BedEventToPlainTextConverter : HttpMessageConverter<BedEvent> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean = false

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return BedEvent::class.java.isAssignableFrom(clazz) &&
            (
            mediaType?.isCompatibleWith(MediaType.TEXT_PLAIN) == true
            ||
            mediaType?.isCompatibleWith(MediaType.TEXT_EVENT_STREAM) == true
            )
    }

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(MediaType.TEXT_PLAIN)

    override fun read(clazz: Class<out BedEvent>, inputMessage: HttpInputMessage): BedEvent {
        throw UnsupportedOperationException("Reading BedEvent from Text is not supported")
    }

    override fun write(bedCellPlanted: BedEvent, contentType: MediaType?, outputMessage: HttpOutputMessage) {
        val text = BedEventHtmlFormatter.convert(bedCellPlanted)
        outputMessage.headers.contentType = MediaType.TEXT_PLAIN
        outputMessage.body.write(text.toByteArray())
    }
}