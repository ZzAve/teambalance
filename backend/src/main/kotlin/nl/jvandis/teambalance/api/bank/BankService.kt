package nl.jvandis.teambalance.api.bank

import dev.hsbrysk.caffeine.CoroutineLoadingCache
import kotlinx.coroutines.runBlocking
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import nl.jvandis.teambalance.api.ConfigurationService
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment
import nl.jvandis.teambalance.api.setupCache
import nl.jvandis.teambalance.api.users.User
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
    private val balanceCache: CoroutineLoadingCache<Tenant, String> =
        setupCache(bankConfig.cache.balance) { _ ->
            updateBalance(getAccountId())
        }

    private val transactionsCache: CoroutineLoadingCache<Tenant, Transactions> =
        setupCache(bankConfig.cache.transactions) { _ ->
            updateTransactions(getAccountId())
        }

    suspend fun getBalance(): String = balanceCache.get(MultiTenantContext.getCurrentTenant())

    suspend fun getTransactions(
        limit: Int = bankConfig.transactionLimit,
        offset: Int = 0,
    ): Transactions =
        transactionsCache
            .get(MultiTenantContext.getCurrentTenant())
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
            .let { transactions ->
                val aliases = getAllAliases()
                val exclusions = getAllTransactionExclusions()
                val transactions =
                    transactions
                        .filter { it.date > configurationService.getStartOfSeasonZoned() }
                        .filter { !it.matchesExclusionCriteria(exclusions) }
                        .map { it.enrichWithAliasFrom(aliases) }

                Transactions(
                    transactions = transactions,
                    limit = transactions.size,
                )
            }
    }

    /**
     * Determines whether the transaction should be excluded based on the provided list
     * of transaction exclusions. Exclusion conditions are checked against transaction
     * ID, date, description, and counterparty details.
     *
     * @param exclusions A list of TransactionExclusion objects that represent the exclusion
     * criteria, including optional transaction ID, date, description, and counterparty details.
     */
    private fun Transaction.matchesExclusionCriteria(exclusions: List<TransactionExclusion>) =
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
