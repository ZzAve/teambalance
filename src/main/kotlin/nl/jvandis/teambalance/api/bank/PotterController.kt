package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.filters.InvalidDateTimeException
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
import java.time.ZonedDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@Validated
@Tags(value = [Tag(name = "aliases"), Tag(name = "bank")])
@RequestMapping(path = ["/api/bank"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PotterController(
    private val potterService: PotterService,
    private val bankConfig: BankConfig
) {

    @GetMapping("/potters")
    fun getPotters(
        @RequestParam(value = "limit", defaultValue = "3") @Max(200) @Min(1) limit: Int,
        @RequestParam(value = "sort", defaultValue = "desc") sort: Sort,
        @RequestParam(value = "since", defaultValue = "2021-08-01T00:00:00+02:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) sinceInput: ZonedDateTime,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean
    ): PottersResponse {

        if (sinceInput < bankConfig.dateTimeLimit) {
            throw InvalidDateTimeException("Since input argument is before lower limit of ${bankConfig.dateTimeLimit}. Input was $sinceInput")
        }
        val pottersFullPeriod = potterService.getPotters(sinceInput, includeInactiveUsers)
        val now = ZonedDateTime.now()
        val pottersLastMonthResponse: PottersResponse? = when {
            Duration.between(sinceInput, now) > Duration.ofDays(30) ->
                potterService.getPotters(now.minusDays(30), includeInactiveUsers)
                    .toPottersResponse(limit)
            else -> null
        }
        return pottersFullPeriod.toPottersResponse(limit, pottersLastMonthResponse)
    }

    private fun Potters.toPottersResponse(limit: Int, pottersLastMonthResponse: PottersResponse? = null) =
        PottersResponse(
            toppers = this.potters.map { it.toPotterResponse(currency) }.sortedByDescending { it.amount }.take(limit),
            floppers = this.potters.map { it.toPotterResponse(currency) }.sortedBy { it.amount }.take(limit),
            amountOfConsideredTransactions = amountOfTransactions,
            from = from,
            until = until,
            subPeriod = pottersLastMonthResponse
        )

    private fun Potter.toPotterResponse(currency: String): PotterResponse {
        val cumulativeAmount = transactions.fold(0.0) { acc, cur -> acc + cur.amount.toDouble() }
        return PotterResponse(
            name = name,
            currency = currency,
            amount = cumulativeAmount
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleSecretExceptions(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message ?: "Bad request"
                )
            )
}
