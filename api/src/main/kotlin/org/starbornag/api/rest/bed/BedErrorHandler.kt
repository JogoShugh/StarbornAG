package org.starbornag.api.rest.bed

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.starbornag.api.application.bed.UnknownBed
import org.starbornag.api.domain.bed.BedAlreadyExists
import org.starbornag.api.domain.bed.CellAlreadyPlanted
import org.starbornag.api.domain.bed.FocusOutsideBed
import org.starbornag.api.domain.bed.LocationOutsideBed
import org.starbornag.api.domain.bed.NothingToHarvest
import java.util.*

class UnknownBedCell(bedId: UUID, bedCellId: UUID) : NoSuchElementException("Bed $bedId has no cell $bedCellId")

/** A form was sent without some of the fields its schema requires. */
class FormIncomplete(val missing: List<String>) :
    IllegalArgumentException("The form is missing ${missing.joinToString(", ")}")

/**
 * An error as HAL Schema Forms requires it: a vnd.error document (github.com/blongden/vnd.error) with a
 * message, one embedded error per field (by JSON pointer [path]), and links to what it is about.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VndError(
    val message: String,
    val path: String? = null,
    @get:JsonProperty("_links") val links: Map<String, Map<String, String>>? = null,
    @get:JsonProperty("_embedded") val embedded: Map<String, List<VndError>>? = null
) {
    companion object {
        val MEDIA_TYPE = MediaType("application", "vnd.error+json")
    }
}

/**
 * Turns the domain's and use cases' rejections into HTTP statuses and vnd.error documents. Each error
 * links "about" the resource to re-read: the focus a refused form was posted from, or the bed when
 * the focus asked for is not part of it.
 */
@RestControllerAdvice
class BedErrorHandler {

    @ExceptionHandler(UnknownBed::class)
    fun unknownBed(e: UnknownBed) = error(HttpStatus.NOT_FOUND, e.message, "/beds")

    @ExceptionHandler(UnknownBedCell::class, FocusOutsideBed::class)
    fun notInTheBed(e: NoSuchElementException, request: HttpServletRequest) =
        error(HttpStatus.NOT_FOUND, e.message, bedOf(request))

    @ExceptionHandler(LocationOutsideBed::class)
    fun locationOutsideBed(e: LocationOutsideBed, request: HttpServletRequest) =
        error(HttpStatus.BAD_REQUEST, e.message, aboutOf(request))

    /** The command is not possible in the cells' current state: re-read the focus and its forms. */
    @ExceptionHandler(CellAlreadyPlanted::class, NothingToHarvest::class)
    fun notPossibleNow(e: IllegalStateException, request: HttpServletRequest) =
        error(HttpStatus.CONFLICT, e.message, aboutOf(request))

    @ExceptionHandler(BedAlreadyExists::class)
    fun bedAlreadyExists(e: BedAlreadyExists, request: HttpServletRequest) =
        error(HttpStatus.CONFLICT, e.message, request.requestURI)

    @ExceptionHandler(FormIncomplete::class)
    fun formIncomplete(e: FormIncomplete, request: HttpServletRequest) =
        error(HttpStatus.BAD_REQUEST, e.message, aboutOf(request), e.missing.map { "/$it" to "$it is required" })

    /** A body that does not fit the command, such as a missing field the form did not catch first. */
    @ExceptionHandler(MismatchedInputException::class, HttpMessageNotReadableException::class)
    fun unreadable(e: Exception, request: HttpServletRequest): ResponseEntity<VndError> {
        val mismatch = e as? MismatchedInputException ?: e.cause as? MismatchedInputException
        val path = mismatch?.path?.mapNotNull { it.fieldName }?.joinToString("/", prefix = "/")
        val fields = if (path.isNullOrEmpty() || path == "/") emptyList() else listOf(path to "$path does not fit")
        return error(HttpStatus.BAD_REQUEST, "The request body does not fit the form", aboutOf(request), fields)
    }

    private fun error(
        status: HttpStatus,
        message: String?,
        about: String,
        fields: List<Pair<String, String>> = emptyList()
    ): ResponseEntity<VndError> = ResponseEntity.status(status).contentType(VndError.MEDIA_TYPE).body(
        VndError(
            message = message ?: status.reasonPhrase,
            links = mapOf("about" to mapOf("href" to about)),
            embedded = fields.takeIf { it.isNotEmpty() }
                ?.let { mapOf("errors" to it.map { (path, text) -> VndError(text, path) }) }
        )
    )

    /** A form posted to ".../focus/{path}/{action}" is about that focus; anything else about itself. */
    private fun aboutOf(request: HttpServletRequest): String {
        val uri = request.requestURI
        return if (request.method == "POST" && FOCUS.matches(uri)) uri.substringBeforeLast("/") else uri
    }

    /** The bed an address under /beds/{id} belongs to. */
    private fun bedOf(request: HttpServletRequest): String =
        BED.find(request.requestURI)?.value ?: request.requestURI

    private companion object {
        val FOCUS = Regex("^/beds/[^/]+/focus/.+$")
        val BED = Regex("^/beds/[^/]+")
    }
}
