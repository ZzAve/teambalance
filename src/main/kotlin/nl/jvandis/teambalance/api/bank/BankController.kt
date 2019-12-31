package nl.jvandis.teambalance.api.bank

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.constraints.Max
import javax.validation.constraints.Min

private const val SECRET_HEADER = "X-Secret"

@RestController
@Validated
@RequestMapping(path = ["/api/bank"])
class BankController(
        private val bankService: BankService,
        @Value("\${app.bank.secret-value}") private val validSecretValue: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/balance")
    fun getBalance(
            @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): BalanceResponse {
        ensureSecret(secret)

        return bankService.getBalance().toResponse()
    }

    @GetMapping("/transactions")
    fun getTransactions(
            @RequestHeader(value = SECRET_HEADER, required = false) secret: String?,
            @RequestParam(value = "limit", defaultValue = "10") @Max(50) @Min(1) limit: Int
    ): TransactionsResponse {
        ensureSecret(secret)


        return bankService.getTransactions(limit).let {
            TransactionsResponse(transactions = it.transactions.toResponse())
        }
    }

    private fun ensureSecret(secret: String?) {
        val decodedSecret = decodeSecret(secret)
        if (decodedSecret != validSecretValue) {
            throw InvalidSecretException("There was no secret provided, or value was not valid")
        }
    }

    private fun decodeSecret(secret: String?): String? = secret?.let {
        try {
            val decoded = Base64.getDecoder().decode(it)
            String(decoded, Charsets.UTF_8)
        } catch (t: Throwable) {
            log.error("Could not parse secret because of encoding issue ($secret)", t)
            null
        }
    }


    private fun String.toResponse() = BalanceResponse(this)

    @ExceptionHandler(InvalidSecretException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    fun handleSecretExceptions(e: InvalidSecretException) = Error(
            status = HttpStatus.FORBIDDEN,
            reason = e.message ?: "Forbidden"
    )

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException) = Error(
            status = HttpStatus.BAD_REQUEST,
            reason = e.message ?: "Please verify your input arguments"
    )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadInputArguments(e: MethodArgumentTypeMismatchException): Error {
        log.info("Invalid request arguments received: ", e.message)
        return Error(
                status = HttpStatus.BAD_REQUEST,
                reason = "Please verify your input arguments"
        )
    }


    @ExceptionHandler(Throwable::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnhandledExceptions(t: Throwable): Error {
        log.error("Unhandled exception occured: ${t.message}", t)
        return Error(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                reason = "Something went wrong. Please try again later"
        )
    }
}

private fun  List<Transaction>.toResponse() = map{
        TransactionResponse(
                id = it.id,
                amount = it.amount,
                counterParty = it.counterParty,
                timestamp = it.date.toEpochSecond()
        )
    }



class InvalidSecretException(msg: String) : RuntimeException(msg)

