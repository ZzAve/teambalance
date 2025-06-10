package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.Config
import com.bunq.sdk.Context
import com.bunq.sdk.Signing
import com.bunq.sdk.generated.Sdk
import com.bunq.sdk.generated.endpoint.READ_MonetaryAccountBank_for_User
import com.bunq.sdk.generated.model.Amount
import com.bunq.sdk.handler
import com.bunq.sdk.initContext
import java.io.File

class BunqRepo(
    apikey: String,
) {
    private val sdk: Sdk
    private val context: Context
    private val signing: Signing

    init {
        val config =
            Config(
                serviceName = "bunq-sdk-teambalance",
                apiKey = apikey,
                publicKeyFile = File("./public_key.pem"),
                privateKeyFile = File("./private_key.pem"),
//            userAgent = TODO(),
//            cacheControl = TODO(),
//            language = TODO(),
//            region = TODO(),
//            clientRequestId = TODO(),
//            geolocation = TODO()
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

    suspend fun getMonetaryAccountBank(accountId: Long): String {
        val readMonetaryaccountbankForUser = sdk.rEAD_MonetaryAccountBank_for_User(context.userId, accountId)
        return when (readMonetaryaccountbankForUser) {
            is READ_MonetaryAccountBank_for_User.Response200 -> {
                val balancePreferred =
                    readMonetaryaccountbankForUser.body.monetary_account_profile
                        ?.profile_fill
                        ?.balance_preferred
                balancePreferred?.let { "${it.parseCurrency()} ${it.value}" } ?: "Unknown"
            }

            is READ_MonetaryAccountBank_for_User.Response400 -> TODO()
        }
    }

    fun Amount.parseCurrency() = if (currency == "EUR") "€" else currency
}
