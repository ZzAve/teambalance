package nl.jvandis.teambalance.api.bank

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import kotlinx.coroutines.runBlocking
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import nl.jvandis.teambalance.api.ConfigurationService
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment
import nl.jvandis.teambalance.api.setupCache
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.filters.START_OF_SEASON_ZONED
import org.springframework.stereotype.Service
import kotlin.math.min

/**
 * BankService resolves bank-related questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqRepository to connect to the bunqAPI
 */
@Service
class BankService(
    private val bunqRepo: BunqRepo,
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val transactionExclusionRepository: BankAccountTransactionExclusionRepository,
    private val bankConfig: BankConfig,
    private val configurationService: ConfigurationService,
) {
    private val balanceCache: AsyncLoadingCache<Tenant, String> =
        setupCache(bankConfig.cache.balance) { tenant: Tenant ->
            updateBalance(getAccountId())
        }

    private val transactionsCache: AsyncLoadingCache<Tenant, Transactions> =
        setupCache(bankConfig.cache.transactions) { tenant: Tenant ->
            updateTransactions(getAccountId())
        }

    fun getBalance(): String = balanceCache[MultiTenantContext.getCurrentTenant()].get()

    fun getTransactions(
        limit: Inmakefit = bankConfig.transactionLimit,
        offset: Int = 0,
    ): Transactions =
        transactionsCache[MultiTenantContext.getCurrentTenant()]
            .get()
            .filter(limit, offset)

    private suspend fun updateBalance(accountId: Int): String {
        val validatedAccountId: Int = getValidatedAccountId(accountId)
        return bunqRepo.getAccountBalance(validatedAccountId.toLong())
    }

    val accountIds = mutableMapOf<Int, Int>()

    // TODO accountId validation fix (service shouldn't care about bunq environment)
    private suspend fun getValidatedAccountId(accountId: Int): Int =
        if (bankConfig.bunq.environment == BunqEnvironment.PRODUCTION) {
            accountId
        } else {
            accountIds.computeIfAbsent(accountId) { id ->
                val bankAccounts = runBlocking { bunqRepo.listMonetaryAccountBank() }
                bankAccounts.firstOrNull()?.id?.toInt() ?: error("Account with id $id not found")
            }
        }

    private suspend fun updateTransactions(accountId: Int): Transactions {
        val validatedAccountId: Int = getValidatedAccountId(accountId)
        return bunqRepo
            .getTransactions(validatedAccountId.toLong())
            .let {
                val aliases = getAllAliases()
                val exclusions = getAllTransactionExclusions()
                val transactions =
                    it
                        .filter { p -> !p.shouldBeExcluded(exclusions) }
                        .map { transaction -> transaction.enrichWithAliasFrom(aliases) }
                        .filter { t -> t.transaction.date > START_OF_SEASON_ZONED }

                Transactions(
                    transactions = transactions,
                    limit = transactions.size,
                )
            }
    }

    private fun Transaction.shouldBeExcluded(exclusions: List<TransactionExclusion>) =
        exclusions.any { e ->
            (e.transactionId == null || id == e.transactionId) &&
                (e.date == null || this.date.toLocalDate().isEqual(e.date)) &&
                (e.description == null || this.description == e.description) &&
                (e.counterParty == null || counterParty.displayName == e.counterParty || counterParty.iban == e.counterParty)
        }

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

    private fun getAllAliases() =
        bankAccountAliasRepository
            .findAll()
            .associate { it.alias to it.user }

    private fun getAllTransactionExclusions() =
        transactionExclusionRepository
            .findAll()

    private fun getAccountId(): Int {
        val key = "bunq-account-id"
        return configurationService.getConfig(key, Int::class)
    }
}

private fun Transaction.enrichWithAliasFrom(aliases: Map<String, User>): TransactionWithAlias =
    TransactionWithAlias(
        this,
        this.counterParty.getAlias(aliases),
    )

private fun CounterParty.getAlias(aliases: Map<String, User>): User? = aliases[this.displayName] ?: aliases[this.iban]
