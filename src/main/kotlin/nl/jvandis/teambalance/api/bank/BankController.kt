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
    ): String {
        ensureSecret(secret)

        return bankService.getBalance()
    }

    @GetMapping("/transactions")
    fun getTransactions(
            @RequestHeader(value = SECRET_HEADER, required = false) secret: String?,
            @RequestParam(value = "limit", defaultValue = "10") @Max(50) @Min(1) limit: Int
    ): TransactionsResponse {
        ensureSecret(secret)


        return bankService.getTransactions(limit).let {
            TransactionsResponse(transactions = it.transactions)
        }
    }

    private fun ensureSecret(secret: String?): String = secret
            ?.let {
                val decoded = Base64.getDecoder().decode(it)
                String(decoded, Charsets.UTF_8)
            }
            .let {
                if (it != validSecretValue) {
                    throw InvalidSecretException("There was no secret provided, or value was not valid")
                }
                it
            }

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

class InvalidSecretException(msg: String) : RuntimeException(msg)

