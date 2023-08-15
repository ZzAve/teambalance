package nl.jvandis.teambalance.api.bank

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

data class CacheConfig(
    val enabled: Boolean = true,
    val expireAfterWrite: Duration,
    val refreshAfterWrite: Duration?
)

data class BankCacheConfig(
    val balance: CacheConfig,
    val transactions: CacheConfig
)

data class BankBunqConfig(
    val apiKey: String?,
    val bankAccountId: Int?,
    val environment: BunqEnvironment,
    val saveSessionToFile: Boolean = false
)

enum class BunqEnvironment {
    PRODUCTION,
    SANDBOX
}

@ConfigurationProperties("app.bank")
data class BankConfig(
    val bunq: BankBunqConfig,
    val cache: BankCacheConfig,
    val transactionLimit: Int
)
