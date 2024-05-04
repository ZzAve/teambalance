package nl.jvandis.teambalance.api.bank

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.filters.TenantsConfig
import nl.jvandis.teambalance.loggerFor
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Validated
@Tag(name = "bank")
@RequestMapping(path = ["/api/bank"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BankController(
    private val bankService: BankService,
    private val tenantsConfig: TenantsConfig,
) {
    private val log = loggerFor()

    @PostMapping("/top-up")
    fun addMoneyToBankAccount(
        @RequestBody topUpRequestBody: TopUpRequestBody,
    ): TopUpRedirectResponse {
        val currentTenant = MultiTenantContext.getCurrentTenant()
        val tenantConfig = tenantsConfig.tenants.first { it.tenant == currentTenant }

        val url =
            if (topUpRequestBody.amountInCents != null) {
                val topUpAmount = topUpRequestBody.amountInCents / 100.0
                val defaultTopUpDescription = "Meer%20Muntjes%20Meer%20Beter"
                val topUpUrl = "${tenantConfig.bunqMeBaseUrl}/$topUpAmount/$defaultTopUpDescription"
                log.info("Creating top-up link for $topUpAmount euro -> $topUpUrl")
                topUpUrl
            } else {
                val topUpUrl = tenantConfig.bunqMeBaseUrl
                log.info("Creating top-up link for a free amount to $topUpUrl")
                topUpUrl
            }
        return TopUpRedirectResponse(URI.create(url).toString())
    }

    @GetMapping("/balance")
    fun getBalance(): BalanceResponse {
        return bankService.getBalance().toResponse()
    }

    @GetMapping("/transactions")
    fun getTransactions(
        @RequestParam(defaultValue = "10")
        @Max(50)
        @Min(1)
        limit: Int,
        @RequestParam(defaultValue = "0")
        @Max(1000)
        @Min(0)
        offset: Int,
    ): TransactionsResponse {
        return bankService.getTransactions(limit, offset).let {
            val transactionResponses = it.transactions.toResponse()
            TransactionsResponse(transactions = transactionResponses)
        }
    }

    private fun String.toResponse() = BalanceResponse(this)

    private fun List<Transaction>.toResponse() =
        map {
            TransactionResponse(
                id = it.id,
                type = it.type,
                // non breakable whitespace
                amount = "${it.currency}\u00A0${it.amount}",
                counterParty = it.user?.name ?: it.counterParty,
                timestamp = it.date.toEpochSecond(),
            )
        }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleSecretExceptions(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message ?: "Bad request",
                ),
            )
}

data class TopUpRequestBody(
    val amountInCents: Int?,
)

data class TopUpRedirectResponse(
    val url: String,
)
