package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import org.slf4j.LoggerFactory
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
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@Validated
@Tag(name = "bank")
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
        @RequestParam(value = "limit", defaultValue = "10") @Max(50) @Min(1) limit: Int,
        @RequestParam(value = "offset", defaultValue = "0") @Max(1000) @Min(0) offset: Int
    ): TransactionsResponse {
        secretService.ensureSecret(secret)

        return bankService.getTransactions(limit, offset).let {
            val transactionResponses = it.transactions.toResponse()
            TransactionsResponse(transactions = transactionResponses)
        }
    }

    private fun String.toResponse() = BalanceResponse(this)

    private fun List<Transaction>.toResponse() = map {
        TransactionResponse(
            id = it.id,
            type = it.type,
            amount = "${it.currency}\u00A0${it.amount}", // non breakable whitespace
            counterParty = it.user?.name ?: it.counterParty,
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
