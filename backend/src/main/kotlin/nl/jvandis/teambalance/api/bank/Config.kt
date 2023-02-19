package nl.jvandis.teambalance.api.bank

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.time.ZonedDateTime

data class CacheConfig(
    val enabled: Boolean = true,
    val expireAfterWrite: Duration,
    val refreshAfterWrite: Duration?,
    val maximumSize: Long
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
    val transactionLimit: Int,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) val dateTimeLimit: ZonedDateTime
)
