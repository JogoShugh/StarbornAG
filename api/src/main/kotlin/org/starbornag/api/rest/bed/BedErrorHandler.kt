package org.starbornag.api.rest.bed

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.starbornag.api.application.bed.UnknownBed
import org.starbornag.api.domain.bed.BedAlreadyExists
import org.starbornag.api.domain.bed.CellAlreadyPlanted
import org.starbornag.api.domain.bed.NothingToHarvest
import org.starbornag.api.domain.bed.LocationOutsideBed
import java.util.*

class UnknownBedCell(bedId: UUID, bedCellId: UUID) : NoSuchElementException("Bed $bedId has no cell $bedCellId")

/** Turns the domain's and use cases' rejections into HTTP statuses. */
@RestControllerAdvice
class BedErrorHandler {

    @ExceptionHandler(UnknownBed::class)
    fun unknownBed(e: UnknownBed): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(UnknownBedCell::class)
    fun unknownBedCell(e: UnknownBedCell): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(LocationOutsideBed::class)
    fun locationOutsideBed(e: LocationOutsideBed): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    /** The command is not possible in the cells' current state (HEART: 409, re-sync and re-derive). */
    @ExceptionHandler(CellAlreadyPlanted::class, NothingToHarvest::class)
    fun notPossibleNow(e: IllegalStateException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler(BedAlreadyExists::class)
    fun bedAlreadyExists(e: BedAlreadyExists): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}
