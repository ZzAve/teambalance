package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.CacheConfig
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for configuring bank-related settings in the application.
 *
 * @property bunq configuration related to Bunq bank services.
 * @property cache configuration for caching bank data like balances and transactions.
 * @property transactionLimit defines the maximum number of transactions to fetch or process.
 */
@ConfigurationProperties("app.bank")
data class BankConfig(
    val bunq: BankBunqConfig,
    val cache: BankCacheConfig,
    val transactionLimit: Int,
) {
    /**
     * Configuration for Bunq bank service.
     *
     * @property apiKey the API key used for authentication with the Bunq service.
     * @property bankAccountId the ID of the bank account associated with Bunq services.
     * @property environment the environment setting for Bunq (e.g., PRODUCTION or SANDBOX).
     * @property saveSessionToFile flag indicating whether to save the session to a file.
     */
    data class BankBunqConfig(
        val apiKey: String?,
        val bankAccountId: Int?,
        val environment: BunqEnvironment,
        val saveSessionToFile: Boolean = false,
    )

    /**
     * Configuration for caching bank-related data.
     *
     * @property balance configuration for caching balance data.
     * @property transactions configuration for caching transaction data.
     */
    data class BankCacheConfig(
        val balance: CacheConfig,
        val transactions: CacheConfig,
    )

    enum class BunqEnvironment {
        PRODUCTION,
        SANDBOX,
    }
}
