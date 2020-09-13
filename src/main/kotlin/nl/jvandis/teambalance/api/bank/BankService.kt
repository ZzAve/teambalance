package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.endpoint.Payment
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Value
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))


@ConfigurationProperties("app.bank.cache")
data class BankCacheConfig @ConfigurationInject constructor(
    val balance: CacheConfig,
    val transactions: CacheConfig
) {
    @ConfigurationProperties("balance") // FIXME: configuration is always from 'balance' instead of by propertyName
    data class CacheConfig @ConfigurationInject constructor(
        val enabled: Boolean = true,
        val expireAfterWrite: Duration,
        val refreshAfterWrite: Duration?,
        val maximumSize: Long
    )
}

/**
 * BankService resolves bankrelated questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqReposity to connect to the bunqAPI
 */
@Singleton
class BankService(
    @Inject private val bunqRepository: BunqRepository,
    @Inject private val bankCacheConfig: BankCacheConfig,
    @Value("\${app.bank.bank-account-id}") private val bankAccountId: Int,
    @Value("\${app.bank.transaction-limit}") private val transactionLimit: Int

) {
    private val balanceCache: AsyncLoadingCache<Int, String> =
        setupCache(bankCacheConfig.balance) { accountId: Int -> updateBalance(accountId) }

    private val transactionsCache: AsyncLoadingCache<Int, Transactions> =
        setupCache(bankCacheConfig.transactions) { accountId: Int -> updateTransactions(accountId) }

    fun getBalance(): String = balanceCache[bankAccountId].get()

    fun getTransactions(limit: Int, offset: Int = 0): Transactions =
        transactionsCache[bankAccountId].get()
            .filter(limit, offset)

    private fun updateBalance(accountId: Int): String {
        return bunqRepository.getMonetaryAccountBank(accountId).balance
            .let {
                parseAmount(it)
            }.also {
                bunqRepository.updateContext()
            }
    }

    private fun updateTransactions(accountId: Int): Transactions {
        return bunqRepository.getAllPayment(accountId, transactionLimit).let {
            Transactions(
                transactions = it.map { payment -> payment.toDomain() },
                limit = it.size
            )
        }.also {
            bunqRepository.updateContext()
        }
    }

    private fun Payment.toDomain() = Transaction(
        id = id,
        amount = parseAmount(amount),
        counterParty = counterpartyAlias.displayName,
        date = created.toZonedDateTime()
    )

    private fun parseAmount(balance: Amount): String {
        val currencySymbol = if (balance.currency == "EUR") "€" else balance.currency
        return "$currencySymbol ${balance.value}"
    }

    private fun String.toZonedDateTime() =
        ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(ZoneId.of("Europe/Paris"))

    private fun Transactions.filter(limit: Int, offset: Int): Transactions {
        val allowedUpperLimit = min(transactions.size, offset + limit)
        val allowedLowerLimit = min(transactions.size - 1, offset)
        val filteredTransactions = transactions.subList(allowedLowerLimit, allowedUpperLimit)
        return Transactions(
            transactions = filteredTransactions,
            limit = filteredTransactions.size
        )
    }

    private fun <K, V> setupCache(config: BankCacheConfig.CacheConfig, loadingFunction: (K) -> V): AsyncLoadingCache<K, V> =
        Caffeine.newBuilder()
            .expireAfterWrite(config.expireAfterWrite)
            .apply { if (config.refreshAfterWrite != null) refreshAfterWrite(config.refreshAfterWrite) }
            .maximumSize(if (config.enabled) config.maximumSize else 0)
            .buildAsync<K, V> { key -> loadingFunction(key) }
}
