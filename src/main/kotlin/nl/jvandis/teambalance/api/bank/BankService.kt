package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.endpoint.Payment
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.min

private const val TEAM_BANK_ACCOUNT_ID = 1547165
private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))
private const val DEFAULT_LIMIT = 200

/**
 * BankService resolves bankrelated questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqReposity to connect to the bunqAPI
 */
@Service
class BankService(
    private val bunqRepository: BunqRepository
) {
    private val balanceCache: Cache<Int, String> = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .maximumSize(1)
        .build()

    private val transactionsCache: Cache<Int, Transactions> = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .maximumSize(1)
        .build()

    fun getBalance(): String = balanceCache.getIfPresent(TEAM_BANK_ACCOUNT_ID) ?: updateBalance()

    private fun updateBalance(): String =
        bunqRepository.getMonetaryAccountBank(TEAM_BANK_ACCOUNT_ID).balance
            .let {
                parseAmount(it)
            }.also {
                balanceCache.put(TEAM_BANK_ACCOUNT_ID, it)
                bunqRepository.updateContext()
            }

    fun getTransactions(limit: Int, offset: Int = 0): Transactions =
        (transactionsCache.getIfPresent(TEAM_BANK_ACCOUNT_ID) ?: updateTransactions())
            .filter(limit, offset)

    private fun updateTransactions(): Transactions =
        bunqRepository.getAllPayment(TEAM_BANK_ACCOUNT_ID, DEFAULT_LIMIT).let {
            Transactions(
                transactions = it.map { payment -> payment.toDomain() },
                limit = it.size
            )
        }.also {
            transactionsCache.put(TEAM_BANK_ACCOUNT_ID, it)
            bunqRepository.updateContext()
        }

    private fun parseAmount(balance: Amount): String {
        val currencySymbol = if (balance.currency == "EUR") "€" else balance.currency
        return "$currencySymbol ${balance.value}"
    }

    private fun Payment.toDomain() = Transaction(
        id = id,
        amount = parseAmount(amount),
        counterParty = counterpartyAlias.displayName,
        date = created.toZonedDateTime()
    )

    private fun String.toZonedDateTime() = ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(ZoneId.of("Europe/Paris"))
}

private fun Transactions.filter(limit: Int, offset: Int): Transactions {
    // filter limit
    val allowedUpperLimit = min(transactions.size, offset + limit)
    val allowedLowerLimit = min(transactions.size - 1, offset)
    val filteredTransactions = transactions.subList(allowedLowerLimit, allowedUpperLimit)
    return Transactions(
        transactions = filteredTransactions,
        limit = filteredTransactions.size
    )
}
