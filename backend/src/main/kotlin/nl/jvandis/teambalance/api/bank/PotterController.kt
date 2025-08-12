package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.filters.START_OF_SEASON_RAW
import nl.jvandis.teambalance.filters.toZonedDateTime
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

@RestController
@Validated
@Tags(value = [Tag(name = "aliases"), Tag(name = "bank")])
@RequestMapping(path = ["/api/bank"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PotterController(
    private val potterService: PotterService,
) {
    @GetMapping("/potters")
    fun getPotters(
        @RequestParam(defaultValue = "3")
        @Max(200)
        @Min(1)
        limit: Int,
        @RequestParam(defaultValue = "desc") sort: Sort,
        @RequestParam(value = "since", defaultValue = START_OF_SEASON_RAW)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        sinceInput: LocalDateTime,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(value = "include-support-roles", defaultValue = "false") includeSupportRoles: Boolean,
    ): PottersResponse {
        val pottersFullPeriod = potterService.getPotters(sinceInput.toZonedDateTime(), includeInactiveUsers, includeSupportRoles)
        val now = ZonedDateTime.now()
        val pottersLastMonthResponse: PottersResponse? =
            if (Duration.between(sinceInput, now) > Duration.ofDays(30)) {
                potterService
                    .getPotters(now.minusDays(30), includeInactiveUsers, includeSupportRoles)
                    .toPottersResponse(limit)
            } else {
                null
            }
        return pottersFullPeriod.toPottersResponse(limit, pottersLastMonthResponse)
    }

    private fun Potters.toPottersResponse(
        limit: Int,
        pottersLastMonthResponse: PottersResponse? = null,
    ) = PottersResponse(
        toppers = potters.map { it.toPotterResponse(currency) }.sortedByDescending { it.amount }.take(limit),
        floppers = potters.map { it.toPotterResponse(currency) }.sortedBy { it.amount }.take(limit),
        amountOfConsideredTransactions = amountOfTransactions,
        from = from,
        until = until,
        subPeriod = pottersLastMonthResponse,
    )

    private fun Potter.toPotterResponse(currency: String): PotterResponse {
        val cumulativeAmount = transactions.fold(0.0) { acc, cur -> acc + cur.transaction.amount.toDouble() }
        return PotterResponse(
            name = name,
            role = role,
            currency = currency,
            amount = cumulativeAmount,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleSecretExceptions(e: IllegalArgumentException) =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message ?: "Bad request",
                ),
            )
}
