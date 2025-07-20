package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.model.generated.endpoint.Payment
import com.bunq.sdk.model.generated.`object`.Amount
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import kotlinx.coroutines.runBlocking
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import nl.jvandis.teambalance.api.ConfigurationService
import nl.jvandis.teambalance.api.setupCache
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.filters.START_OF_SEASON_ZONED
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))
val EUROPE_AMSTERDAM: ZoneId = ZoneId.of("Europe/Amsterdam")

/**
 * BankService resolves bank related questions
 *
 * Its main function is to retrieve information from the BunqRepository and make it available in domain objects.
 * Makes use of a BunqRepository to connect to the bunqAPI
 */
@Service
class BankService(
    @Lazy private val bunqRepository: BunqRepository,
    private val bunqRepo: BunqRepo,
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val transactionExclusionRepository: BankAccountTransactionExclusionRepository,
    private val bankConfig: BankConfig,
    private val configurationService: ConfigurationService,
) {
    private val balanceCache: AsyncLoadingCache<Tenant, String> =
        setupCache(bankConfig.cache.balance) { tenant: Tenant ->
            println("--- START ---")
            val updateBalance = updateBalance(getAccountId())
            println(updateBalance)
            println("---")
            val updateBalance2 = updateBalance2(getAccountId())
            println(updateBalance2)
            println("--- DONE ---")
            if (updateBalance != updateBalance2) {
                println("!!! The balance is not equal for the two caches. !!!")
            }
            updateBalance
        }

    private val transactionsCache: AsyncLoadingCache<Tenant, Transactions> =
        setupCache(bankConfig.cache.transactions) { tenant: Tenant ->
            println("--- START ---")
            val updateTransactions = updateTransactions(getAccountId())
            println(updateTransactions.transactions.take(3).joinToString("\n"))
            println("---")
            val updateTransactions2 = updateTransactions2(getAccountId())
            println(updateTransactions2.transactions.take(3).joinToString("\n"))
            println("--- DONE ---")
            if (updateTransactions.transactions.size != updateTransactions2.transactions.size) {
                println("!!! The transaction list is not equal for the two caches. !!!")
            }

            updateTransactions2
        }

    fun getBalance(): String = balanceCache[MultiTenantContext.getCurrentTenant()].get()

    fun getTransactions(
        limit: Int = bankConfig.transactionLimit,
        offset: Int = 0,
    ): Transactions =
        transactionsCache[MultiTenantContext.getCurrentTenant()]
            .get()
            .filter(limit, offset)

    private fun updateBalance(accountId: Int): String =
        bunqRepository
            .getMonetaryAccountBank(accountId)
            .balance
            .let {
                "${it.parseCurrency()} ${it.value}"
            }.also {
                bunqRepository.updateContext()
            }

    private suspend fun updateBalance2(accountId: Int): String {
        val validatedAccountId: Int = getValidatedAccountId(accountId)
        return bunqRepo.getAccountBalance(validatedAccountId.toLong()) ?: "Unknown"
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

    // TODO: Fetch all transactions up to datelimit
    private suspend fun updateTransactions(accountId: Int): Transactions =
        bunqRepository
            .getAllPayments(accountId, bankConfig.transactionLimit)
            .let {
                val aliases = getAllAliases()
                val exclusions = getAllTransactionExclusions()
                val transactions =
                    it
                        .filter { p -> !p.shouldBeExcluded(exclusions) }
                        .map { payment -> payment.toDomain(aliases) }
                        .filter { t -> t.transaction.date > START_OF_SEASON_ZONED }

                Transactions(
                    transactions = transactions,
                    limit = transactions.size,
                )
            }.also {
                bunqRepository.updateContext()
            }

    private suspend fun updateTransactions2(accountId: Int): Transactions {
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
                (e.date == null || this.date.toLocalDate() == e.date) &&
                (e.description == null || this.description == e.description) &&
                (e.counterParty == null || counterParty.displayName == e.counterParty || counterParty.iban == e.counterParty)
        }

    private fun Payment.shouldBeExcluded(exclusions: List<TransactionExclusion>) =
        exclusions.any { e ->
            (e.transactionId == null || "$id" == e.transactionId) &&
                (e.date == null || created.toZonedDateTime().toLocalDate().isEqual(e.date)) &&
                (e.description == null || description == e.description) &&
                (e.counterParty == null || counterpartyAlias.displayName == e.counterParty)
        }

    private fun Payment.toDomain(aliases: Map<String, User>): TransactionWithAlias {
        val transaction =
            Transaction(
                id = "$id",
                type = toTransactionType(),
                currency = amount.parseCurrency(),
                amount = amount.value,
                counterParty = CounterParty(counterpartyAlias.iban, counterpartyAlias.displayName),
                date = created.toZonedDateTime(),
                description = description,
            )
        val alias = counterpartyAlias.displayName.getAlias(aliases)
        return TransactionWithAlias(
            transaction,
            alias = alias,
        )
    }

    fun Payment.toTransactionType() = if (amount.value.startsWith("-")) TransactionType.CREDIT else TransactionType.DEBIT

    private fun Amount.parseCurrency() = if (currency == "EUR") "€" else currency

    private fun String.toZonedDateTime() = ZonedDateTime.parse(this, FORMATTER).withZoneSameInstant(EUROPE_AMSTERDAM)

    private fun Instant.roundToClosestMinute(): Instant {
        val seconds = this.epochSecond
        val roundedSeconds = ((seconds + 30) / 60) * 60
        return Instant.ofEpochSecond(roundedSeconds)
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

    private fun String.getAlias(aliases: Map<String, User>): User? = aliases[this]

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
