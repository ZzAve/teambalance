package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.`object`.Amount // ktlint-disable import-ordering
import com.bunq.sdk.model.generated.endpoint.Payment
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import nl.jvandis.teambalance.api.users.User
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))

/**
 * BankService resolves bank related questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqRepository to connect to the bunqAPI
 */
@Service
class BankService(
    @Lazy private val bunqRepository: BunqRepository,
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val transactionExclusionRepository: BankAccountTransactionExclusionRepository,
    private val bankConfig: BankConfig
) {
    private val log = LoggerFactory.getLogger(BankService::class.java)

    private val balanceCache: AsyncLoadingCache<String, String> =
        setupCache(bankConfig.cache.balance) { _: String -> updateBalance() }

    private val transactionsCache: AsyncLoadingCache<String, Transactions> =
        setupCache(bankConfig.cache.transactions) { _: String -> updateTransactions() }

    fun getBalance(): String = balanceCache["teambalance-bank-account"].get()

    fun getTransactions(limit: Int = bankConfig.transactionLimit, offset: Int = 0): Transactions =
        transactionsCache["teambalance-bank-account"].get()
            .filter(limit, offset)

    private fun updateBalance(): String {
        return bunqRepository.getMonetaryAccountBank().balance
            .let {
                "${it.parseCurrency()} ${it.value}"
            }.also {
                bunqRepository.updateContext()
            }
    }

    // TODO: Fetch all transactions up to datelimit
    private fun updateTransactions(): Transactions =
        bunqRepository.getAllPayments(bankConfig.transactionLimit)
            .let {
                val aliases = getAllAliases()
                val exclusions = getAllTransactionExclusions()
                val transactions = it
                    .filter { p -> !p.shouldBeExcluded(exclusions) }
                    .map { payment -> payment.toDomain(aliases) }
                    .filter { t -> t.date > bankConfig.dateTimeLimit }

                Transactions(
                    transactions = transactions,
                    limit = transactions.size
                )
            }
            .also {
                bunqRepository.updateContext()
            }

    private fun Payment.shouldBeExcluded(exclusions: List<TransactionExclusion>) =
        exclusions.any { e ->
            (e.transactionId == null || id == e.transactionId) &&
                (e.date == null || created.toZonedDateTime().toLocalDate() == e.date) &&
                (e.description == null || description == e.description) &&
                (e.counterParty == null || counterpartyAlias.displayName == e.counterParty)
        }

    private fun Payment.toDomain(aliases: Map<String, User>): Transaction {
        return Transaction(
            id = id,
            type = toTransactionType(),
            currency = amount.parseCurrency(),
            amount = amount.value,
            user = counterpartyAlias.displayName.getAlias(aliases),
            counterParty = counterpartyAlias.displayName,
            date = created.toZonedDateTime()
        )
    }

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

    private fun getAllTransactionExclusions() = transactionExclusionRepository
        .findAll()
}
