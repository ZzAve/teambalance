package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.BUNQ_PUBLIC_SERVER
import com.bunq.sdk.BUNQ_SANDBOX_SERVER
import com.bunq.sdk.BunqSigningKeysGenerator
import com.bunq.sdk.Config
import com.bunq.sdk.Context
import com.bunq.sdk.generated.Sdk
import com.bunq.sdk.generated.endpoint.List_all_MonetaryAccountBank_for_User
import com.bunq.sdk.generated.endpoint.List_all_Payment_for_User_MonetaryAccount
import com.bunq.sdk.generated.endpoint.READ_MonetaryAccountBank_for_User
import com.bunq.sdk.generated.model.Amount
import com.bunq.sdk.generated.model.MonetaryAccountBankListing
import com.bunq.sdk.generated.model.PaymentListing
import com.bunq.sdk.handler
import com.bunq.sdk.initContext
import com.bunq.sdk.refreshSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.jvandis.teambalance.api.bank.BankConfig.BankBunqConfig
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment.PRODUCTION
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment.SANDBOX
import nl.jvandis.teambalance.log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

private const val FORMAT_DATE: String = "yyyy-MM-dd HH:mm:ss.SSSSSS"
private val FORMATTER = DateTimeFormatter.ofPattern(FORMAT_DATE)

class BunqRepo(
    private val bunqConfig: BankBunqConfig,
) {
    // Lazily initialized SDK components
    private lateinit var config: Config
    private lateinit var sdk: Sdk
    private lateinit var context: Context

    @Volatile
    private var initialized: Boolean = false

    // Ensure only a single SDK invocation at a time
    private val sdkMutex = Mutex()

    // Ensure initialization happens only once
    private val initMutex = Mutex()

    private suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return@withLock

            log.info("Initializing Bunq SDK (lazy)")
            val initTiming =
                measureTimeMillis {
                    config = getBunqSdkConfig()
                    context = initContext(config)
                    sdk = Sdk(handler(context = context))
                }

            log.info("Done initializing Bunq SDK in ${initTiming}ms")
            initialized = true
        }
    }

    private fun ensureSessionActive() {
        val session = context.sessionExpiryTime
        if (session == null || session.minusSeconds(30).isBefore(Instant.now())) {
            context = context.refreshSession()
            sdk = Sdk(handler(context))
        }
    }

    suspend fun getAccountBalance(accountId: Long): String =
        withBunqContext {
            val monetaryAccountResponse =
                rEAD_MonetaryAccountBank_for_User(
                    userID = context.userId,
                    itemId = accountId,
                )
            when (monetaryAccountResponse) {
                is READ_MonetaryAccountBank_for_User.Response200 -> {
                    monetaryAccountResponse
                        .body
                        .MonetaryAccountBank
                        ?.balance
                        ?.toDomain()
                        ?: "Unknown"
                }

                is READ_MonetaryAccountBank_for_User.Response400 -> TODO()
            }
        }

    private fun Amount.toDomain(): String = "${parseCurrency()} $value"

    suspend fun getTransactions(accountId: Long): List<Transaction> =
        withBunqContext {
            val paymentsResponse =
                list_all_Payment_for_User_MonetaryAccount(
                    userID = context.userId,
                    monetaryaccountID = accountId,
                    count = 200,
                    newer_id = null,
                    older_id = null,
                )

            when (paymentsResponse) {
                is List_all_Payment_for_User_MonetaryAccount.Response200 -> {
                    paymentsResponse.body
                        .filter { it.amount != null && it.created != null && it.counterparty_alias?.display_name != null }
                        .map(PaymentListing::toDomain)
                }

                is List_all_Payment_for_User_MonetaryAccount.Response400 -> TODO()
            }
        }

    suspend fun listMonetaryAccountBank(): List<BankAccount> =
        withBunqContext {
            val bankAccountsResponse =
                list_all_MonetaryAccountBank_for_User(
                    userID = context.userId,
                    count = null,
                    newer_id = null,
                    older_id = null,
                )
            when (bankAccountsResponse) {
                is List_all_MonetaryAccountBank_for_User.Response200 -> {
                    bankAccountsResponse.body.map { it.toDomain() }
                }

                is List_all_MonetaryAccountBank_for_User.Response400 -> TODO()
            }
        }.also { bankAccounts ->
            bankAccounts.forEach {
                if (it.balance.endsWith("0.00")) {
                    log.warn("Found account with 0 balance: ${it.alias} (${it.id}). Requesting spending money")
                    requireSpendingMoney(context.userId, it.id, sdk)
                }
            }
        }

    /**
     * Help function that ensures interaction with the Bunq API is thread safe
     */
    private suspend fun <T> withBunqContext(block: suspend Sdk.() -> T): T {
        ensureInitialized()
        return sdkMutex.withLock {
            ensureSessionActive()
            with(sdk) {
                block()
            }
        }
    }

    private fun getBunqSdkConfig(): Config {
        val signingKeys = BunqSigningKeysGenerator.generateBunqSigningKeys()
        val config =
            when (bunqConfig.environment) {
                PRODUCTION -> {
                    require(!bunqConfig.apiKey.isNullOrBlank()) {
                        "Production environment requires an API key."
                    }

                    Config(
                        bunqServer = BUNQ_PUBLIC_SERVER,
                        serviceName = "bunq-sdk-teambalance",
                        apiKey = bunqConfig.apiKey,
                        signingKeys = signingKeys,
                    )
                }

                SANDBOX -> {
                    val apiKey =
                        when {
                            bunqConfig.apiKey.isNullOrBlank() -> {
                                createSandboxUserApiKey(BUNQ_SANDBOX_SERVER.baseUrl)
                            }

                            bunqConfig.apiKey.startsWith("sandbox") -> {
                                bunqConfig.apiKey
                            }

                            else -> {
                                log.warn(
                                    "Provided API key does not start with 'sandbox'. Falling back to generated sandbox key.",
                                )
                                createSandboxUserApiKey(BUNQ_SANDBOX_SERVER.baseUrl)
                            }
                        }

                    Config(
                        bunqServer = BUNQ_SANDBOX_SERVER,
                        serviceName = "bunq-sdk-teambalance",
                        apiKey = apiKey,
                        signingKeys = signingKeys,
                    )
                }
            }
        return config
    }
}

private fun MonetaryAccountBankListing.toDomain() =
    BankAccount(
        id = id ?: -1,
        balance = balance.toDomain(),
        alias = alias?.filter { it.name != null }?.joinToString { it.name!! },
    )

private fun Amount?.toDomain(): String = if (this == null) "Unknown" else "${parseCurrency()} $value"

private fun PaymentListing.toDomain(): Transaction {
    val amount = this.amount
    val createdDate = this.created
    val counterpartyAlias = this.counterparty_alias
    val displayName = counterpartyAlias?.display_name

    require(amount != null && createdDate != null && displayName != null) {
        "PaymentListing is not valid. It needs an amount, created and counterparty_alias to be valid, " +
            "but received: $this"
    }

    return Transaction(
        id = id?.let { "$it" } ?: "UNKNOWN",
        type = amount.toTransactionType(),
        currency = amount.parseCurrency() ?: "",
        amount = amount.value ?: "",
        counterParty =
            CounterParty(
                iban = counterpartyAlias.iban,
                displayName = displayName,
            ),
        date = createdDate.let { LocalDateTime.parse(it, FORMATTER).atZone(ZoneId.of("UTC")) },
        description = description,
    )
}

private fun Amount.parseCurrency() = if (currency == "EUR") "€" else currency

private fun Amount.toTransactionType(): TransactionType =
    if (value?.startsWith("-") == true) {
        TransactionType.CREDIT
    } else {
        TransactionType.DEBIT
    }
