package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@Validated
@RequestMapping(path = ["/api/bank"])
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
