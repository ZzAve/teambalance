package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.endpoint.Payment
import com.bunq.sdk.model.generated.`object`.Amount
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import nl.jvandis.teambalance.api.ConfigurationService
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.filters.START_OF_SEASON_ZONED
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
    private val bankConfig: BankConfig,
    private val configurationService: ConfigurationService,
) {
    private val balanceCache: AsyncLoadingCache<Tenant, String> =
        setupCache(bankConfig.cache.balance) { tenant: Tenant ->
            val accountId = getAccountId(tenant)
            val updateBalance = updateBalance(accountId)
            updateBalance
        }

    private val transactionsCache: AsyncLoadingCache<Tenant, Transactions> =
        setupCache(bankConfig.cache.transactions) { tenant: Tenant ->
            val accountId = getAccountId(tenant)
            val updatedTransactions = updateTransactions(accountId)
            updatedTransactions
        }

    fun getBalance(): String = balanceCache[MultiTenantContext.getCurrentTenant()].get()

    fun getTransactions(
        limit: Int = bankConfig.transactionLimit,
        offset: Int = 0,
    ): Transactions =
        transactionsCache[MultiTenantContext.getCurrentTenant()].get()
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
    private fun updateTransactions(accountId: Int): Transactions =
        bunqRepository.getAllPayments(accountId, bankConfig.transactionLimit)
            .let {
                val aliases = getAllAliases()
                val exclusions = getAllTransactionExclusions()
                val transactions =
                    it
                        .filter { p -> !p.shouldBeExcluded(exclusions) }
                        .map { payment -> payment.toDomain(aliases) }
                        .filter { t -> t.date > START_OF_SEASON_ZONED }

                Transactions(
                    transactions = transactions,
                    limit = transactions.size,
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

    private fun Payment.toDomain(aliases: Map<String, User>) =
        Transaction(
            id = id,
            type = toTransactionType(),
            currency = amount.parseCurrency(),
            amount = amount.value,
            user = counterpartyAlias.displayName.getAlias(aliases),
            counterParty = counterpartyAlias.displayName,
            date = created.toZonedDateTime(),
        )

    private fun Payment.toTransactionType() = if (amount.value.startsWith("-")) TransactionType.CREDIT else TransactionType.DEBIT

    private fun Amount.parseCurrency() = if (currency == "EUR") "€" else currency

    private fun String.toZonedDateTime() = ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(ZoneId.of("Europe/Paris"))

    private fun Transactions.filter(
        limit: Int,
        offset: Int,
    ): Transactions {
        val allowedUpperLimit = min(transactions.size, offset + limit)
        val allowedLowerLimit = min(transactions.size, offset)
        val filteredTransactions = transactions.subList(allowedLowerLimit, allowedUpperLimit)
        return Transactions(
            transactions = filteredTransactions,
            limit = filteredTransactions.size,
        )
    }

    private fun <K, V> setupCache(
        config: CacheConfig,
        loadingFunction: (K) -> V,
    ): AsyncLoadingCache<K, V> =
        Caffeine.newBuilder()
            .expireAfterWrite(config.expireAfterWrite)
            .apply { if (config.refreshAfterWrite != null) refreshAfterWrite(config.refreshAfterWrite) }
            .maximumSize(if (config.enabled) Tenant.entries.size.toLong() else 0)
            .buildAsync { key -> loadingFunction(key) }

    private fun String.getAlias(aliases: Map<String, User>): User? = aliases[this]

    private fun getAllAliases() =
        bankAccountAliasRepository
            .findAll()
            .associate { it.alias to it.user }

    private fun getAllTransactionExclusions() =
        transactionExclusionRepository
            .findAll()

    private fun getAccountId(tenant: Tenant): Int {
        val key = "bunq-account-id.${tenant.name.lowercase()}"
        return configurationService.getConfig(key, Int::class)
    }
}
