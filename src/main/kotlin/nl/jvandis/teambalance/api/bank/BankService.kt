package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.endpoint.Payment
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import nl.jvandis.teambalance.api.users.User
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Lazy
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))

data class CacheConfig(
    val enabled: Boolean = true,
    val expireAfterWrite: Duration,
    val refreshAfterWrite: Duration?,
    val maximumSize: Long
)

data class BankCacheConfig(
    val balance: CacheConfig,
    val transactions: CacheConfig
)

data class BankBunqConfig(
    val apiKey: String,
    val saveSessionToFile: Boolean = false
)

@ConstructorBinding
@ConfigurationProperties("app.bank")
data class BankConfig(
    val bunq: BankBunqConfig,
    val cache: BankCacheConfig,
    val bankAccountId: Int,
    val transactionLimit: Int,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val dateTimeLimit: ZonedDateTime
)

/**
 * BankService resolves bankrelated questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqReposity to connect to the bunqAPI
 */
@Service
class BankService(
    @Lazy private val bunqRepository: BunqRepository,
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val bankConfig: BankConfig
) {
    private val balanceCache: AsyncLoadingCache<Int, String> =
        setupCache(bankConfig.cache.balance) { accountId: Int -> updateBalance(accountId) }

    private val transactionsCache: AsyncLoadingCache<Int, Transactions> =
        setupCache(bankConfig.cache.transactions) { accountId: Int -> updateTransactions(accountId) }

    fun getBalance(): String = balanceCache[bankConfig.bankAccountId].get()

    fun getTransactions(limit: Int = bankConfig.transactionLimit, offset: Int = 0): Transactions =
        transactionsCache[bankConfig.bankAccountId].get()
            .filter(limit, offset)

    private fun updateBalance(accountId: Int): String {
        return bunqRepository.getMonetaryAccountBank(accountId).balance
            .let {
                "${it.parseCurrency()} ${it.value}"
            }.also {
                bunqRepository.updateContext()
            }
    }

    // TODO: Fetch all transactions up to datelimit
    private fun updateTransactions(accountId: Int): Transactions {
        return bunqRepository.getAllPayment(accountId, bankConfig.transactionLimit).let {
            val aliases = getAllAliases()
            val transactions = it.map { payment -> payment.toDomain(aliases) }
                .filter { t -> t.date > bankConfig.dateTimeLimit }

            Transactions(
                transactions = transactions,
                limit = transactions.size
            )
        }
            .also {
                bunqRepository.updateContext()
            }
    }

    private fun Payment.toDomain(aliases: Map<String, User>) = Transaction(
        id = id,
        type = toTransactionType(),
        currency = amount.parseCurrency(),
        amount = amount.value,
        user = counterpartyAlias.displayName.getAlias(aliases),
        counterParty = counterpartyAlias.displayName,
        date = created.toZonedDateTime()
    )

    private fun Payment.toTransactionType() =
        if (amount.value.startsWith("-")) TransactionType.CREDIT else TransactionType.DEBIT

    private fun Amount.parseCurrency(): String {
        return if (currency == "EUR") "€" else currency
    }

    private fun String.toZonedDateTime() =
        ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(ZoneId.of("Europe/Paris"))

    private fun Transactions.filter(limit: Int, offset: Int): Transactions {
        val allowedUpperLimit = min(transactions.size, offset + limit)
        val allowedLowerLimit = min(transactions.size, offset)
        val filteredTransactions = transactions.subList(allowedLowerLimit, allowedUpperLimit)
        return Transactions(
            transactions = filteredTransactions,
            limit = filteredTransactions.size
        )
    }

    private fun <K, V> setupCache(config: CacheConfig, loadingFunction: (K) -> V): AsyncLoadingCache<K, V> =
        Caffeine.newBuilder()
            .expireAfterWrite(config.expireAfterWrite)
            .apply { if (config.refreshAfterWrite != null) refreshAfterWrite(config.refreshAfterWrite) }
            .maximumSize(if (config.enabled) config.maximumSize else 0)
            .buildAsync { key -> loadingFunction(key) }

    private fun String.getAlias(aliases: Map<String, User>): User? {
        return aliases[this]
    }

    private fun getAllAliases() = bankAccountAliasRepository
        .findAll()
        .associate { it.alias to it.user }
}
