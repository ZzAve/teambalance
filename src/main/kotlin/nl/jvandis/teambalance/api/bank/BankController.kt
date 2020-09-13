package nl.jvandis.teambalance.api.bank

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@Controller(value = "/api/bank", produces = [MediaType.APPLICATION_JSON])
@Validated
@Tag(name = "Bank")
class BankController(
    @Inject private val secretService: SecretService,
    @Inject private val bankService: BankService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Get("/balance")
    fun getBalance(
        @Header(value = SECRET_HEADER) secret: String?
    ): BalanceResponse {
        secretService.ensureSecret(secret)

        return bankService.getBalance().toResponse()
    }

    @Get("/transactions")
    fun getTransactions(
        @Header(value = SECRET_HEADER) secret: String?,
        @QueryValue(value = "limit", defaultValue = "10") @Max(50) @Min(1) limit: Int
    ): TransactionsResponse {
        secretService.ensureSecret(secret)

        return bankService.getTransactions(limit, 0).let {
            TransactionsResponse(transactions = it.transactions.toResponse())
        }
    }

    private fun String.toResponse() = BalanceResponse(this)

    private fun List<Transaction>.toResponse() = map {
        TransactionResponse(
            id = it.id,
            amount = it.amount,
            counterParty = it.counterParty,
            timestamp = it.date.toEpochSecond()
        )
    }
}
