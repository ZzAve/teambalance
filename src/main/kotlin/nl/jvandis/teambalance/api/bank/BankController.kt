package nl.jvandis.teambalance.api.bank

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@Validated
@Api(value = "Bank", tags = ["bank"])
@RequestMapping(path = ["/api/bank"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankController(
    private val bankService: BankService,
    private val secretService: SecretService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/balance")
    fun getBalance(
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): BalanceResponse {
        secretService.ensureSecret(secret)

        return bankService.getBalance().toResponse()
    }

    @GetMapping("/transactions")
    fun getTransactions(
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?,
        @RequestParam(value = "limit", defaultValue = "10") @Max(50) @Min(1) limit: Int
    ): TransactionsResponse {
        secretService.ensureSecret(secret)

        return bankService.getTransactions(limit, 0).let {
            TransactionsResponse(transactions = it.transactions.toResponse())
        }
    }

    @GetMapping("/potters")
    fun getPotters(
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?,
        @RequestParam(value = "limit", defaultValue = "3") @Max(200) @Min(1) limit: Int,
        @RequestParam(value = "sort", defaultValue = "desc") sort: Sort,
        @RequestParam(value = "since", defaultValue = "2021-08-01T00:00:00+02:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) sinceInput: ZonedDateTime
    ): PottersResponse {
        secretService.ensureSecret(secret)

        val sinceLowerLimit = ZonedDateTime.of(2021, 7, 31, 23, 59, 59, 0, ZoneId.of("Europe/Paris"))
        if (sinceInput < sinceLowerLimit){
            throw IllegalArgumentException("Since input argument is before lower limit of $sinceLowerLimit. Input was $sinceInput")
        }
        return bankService.getPotters(sinceInput).toPottersResponse(limit)
    }

    private fun Potters.toPottersResponse(limit: Int) = PottersResponse(
        potters = potters.map { it.toPotterResponse(currency) }.sortedByDescending { it.amount }.take(limit),
        amountOfConsideredTransactions = amountOfTransactions,
        from = from,
        until = until
    )

    private fun Potter.toPotterResponse(currency: String): PotterResponse {
        val cumulativeAmount = transactions.fold(0.0) { acc, cur -> acc + cur.amount.toDouble() }
        return PotterResponse(
            name = name,
            currency = currency,
            amount = cumulativeAmount
        )
    }

    private fun String.toResponse() = BalanceResponse(this)

    private fun List<Transaction>.toResponse() = map {
        TransactionResponse(
            id = it.id,
            type = it.type,
            amount = "${it.currency} ${it.amount}",
            counterParty = it.counterParty,
            timestamp = it.date.toEpochSecond()
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
