package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.BUNQ_PUBLIC_SERVER
import com.bunq.sdk.BUNQ_SANDBOX_SERVER
import com.bunq.sdk.Config
import com.bunq.sdk.Context
import com.bunq.sdk.Signing
import com.bunq.sdk.generated.Sdk
import com.bunq.sdk.generated.endpoint.List_all_MonetaryAccountBank_for_User
import com.bunq.sdk.generated.endpoint.List_all_Payment_for_User_MonetaryAccount
import com.bunq.sdk.generated.endpoint.READ_MonetaryAccountBank_for_User
import com.bunq.sdk.generated.model.Amount
import com.bunq.sdk.generated.model.LabelMonetaryAccount
import com.bunq.sdk.generated.model.MonetaryAccountBankListing
import com.bunq.sdk.generated.model.PaymentListing
import com.bunq.sdk.handler
import com.bunq.sdk.initContext
import nl.jvandis.teambalance.api.bank.BunqEnvironment.PRODUCTION
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val FORMAT_DATE: String = "yyyy-MM-dd HH:mm:ss.SSSSSS"
private val FORMATTER = DateTimeFormatter.ofPattern(FORMAT_DATE)

class BunqRepo(
    bunqConfig: BankBunqConfig,
) {
    private val sdk: Sdk
    private val context: Context
    private val signing: Signing

    init {
        when (bunqConfig.environment) {
            PRODUCTION -> {
                require(bunqConfig.apiKey != null) {
                    "Production environment requires an API key."
                }
                val config =
                    Config(
                        bunqServer = BUNQ_PUBLIC_SERVER,
                        serviceName = "bunq-sdk-teambalance",
                        apiKey = bunqConfig.apiKey,
                        publicKeyFile = File("./public_key.pem").also { it.deleteOnExit() },
                        privateKeyFile = File("./private_key.pem").also { it.deleteOnExit() },
                    )
                signing =
                    Signing(
                        config,
                    )

                context =
                    initContext(
                        config = config,
                    )

                sdk = Sdk(handler(context = context, signing = signing))
            }

            BunqEnvironment.SANDBOX -> {
                require(bunqConfig.apiKey == null || bunqConfig.apiKey.startsWith("sandbox")) {
                    "Sandbox environment API keys always start with 'sandbox'. Yours doesn't, and is most likely a misconfiguration."
                }
                val apiKey = bunqConfig.apiKey ?: createSandboxUserApiKey(BUNQ_SANDBOX_SERVER.baseUrl)
                val config =
                    Config(
                        bunqServer = BUNQ_SANDBOX_SERVER,
                        serviceName = "bunq-sdk-teambalance",
                        apiKey = apiKey,
                        publicKeyFile = File("./public_key.pem").also { it.deleteOnExit() },
                        privateKeyFile = File("./private_key.pem").also { it.deleteOnExit() },
                    )
                signing =
                    Signing(
                        config,
                    )

                context =
                    initContext(
                        config = config,
                    )

                sdk = Sdk(handler(context = context, signing = signing))
            }
        }
    }

    suspend fun getAccountBalance(accountId: Long): String {
        val readMonetaryaccountbankForUser = sdk.rEAD_MonetaryAccountBank_for_User(context.userId, accountId)
        return when (readMonetaryaccountbankForUser) {
            is READ_MonetaryAccountBank_for_User.Response200 -> {
                val balancePreferred =
                    readMonetaryaccountbankForUser.body.MonetaryAccountBank
                        ?.balance
                balancePreferred?.toDomain() ?: "Unknown"
            }

            is READ_MonetaryAccountBank_for_User.Response400 -> TODO()
        }
    }

    private fun Amount.toDomain(): String = "${parseCurrency()} $value"

    suspend fun getTransactions(accountId: Long): List<Transaction> {
        val response = sdk.list_all_Payment_for_User_MonetaryAccount(context.userId, accountId, 200, null, null)
        return when (response) {
            is List_all_Payment_for_User_MonetaryAccount.Response200 -> {
                response.body
                    .filter { it.amount != null && it.created != null && it.counterparty_alias.isValid() }
                    .map(PaymentListing::toDomain)
            }

            is List_all_Payment_for_User_MonetaryAccount.Response400 -> TODO()
        }
    }

    suspend fun listMonetaryAccountBank(): List<BankAccount> {
        val bankAccountsResponse =
            sdk.list_all_MonetaryAccountBank_for_User(context.userId, null, null, null)
        return when (bankAccountsResponse) {
            is List_all_MonetaryAccountBank_for_User.Response200 -> {
                bankAccountsResponse.body.map { it.toDomain() }
            }

            is List_all_MonetaryAccountBank_for_User.Response400 -> TODO()
        }
    }
}

private fun MonetaryAccountBankListing.toDomain() =
    BankAccount(
        id = id ?: -1,
        balance = balance?.parseCurrency() ?: "Unknown",
        alias = alias?.filter { it.name != null }?.joinToString { it.name!! },
    )

private fun PaymentListing.toDomain(): Transaction {
    require(amount != null && created != null && counterparty_alias.isValid()) {
        "PaymentListing is not valid. It needs an amount, created and counterparty_alias to be valid, but received: $this"
    }
    val amount = amount!!
    val created = created!!

    return Transaction(
        id = id?.let { "$it" } ?: "UNKNOWN",
        type = amount.toTransactionType(),
        currency = amount.parseCurrency() ?: "",
        amount = amount.value ?: "",
        counterParty = counterparty_alias.toDomain(),
        date = created.let { LocalDateTime.parse(it, FORMATTER).atZone(ZoneId.of("UTC")) },
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

private fun LabelMonetaryAccount?.toDomain(): CounterParty {
    require(isValid()) { "LabelMonetaryAccount is not valid. It needs a display_name and iban to be valid, but received: $this" }
    return CounterParty(
        iban = this!!.iban,
        displayName = this.display_name!!,
    )
}

private fun LabelMonetaryAccount?.isValid() = this != null && this.display_name != null
