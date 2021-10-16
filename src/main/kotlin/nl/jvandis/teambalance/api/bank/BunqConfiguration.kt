package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.context.ApiEnvironmentType
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Lazy
@Configuration
class BunqConfiguration(
    private val bankConfig: BankConfig
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun bunqLib(): BunqRepository {
        val apiKey = bankConfig.bunq.apiKey
        val saveSessionToFile = bankConfig.bunq.saveSessionToFile

        log.info("Using api-key '${apiKey.substring(0, 5)}******${apiKey.substring(apiKey.length - 5)}'")
        return try {
            BunqRepository(ApiEnvironmentType.PRODUCTION, apiKey, saveSessionToFile)
        } catch (t: Throwable) {
            log.error("Could not create BunqRepo:", t)
            BunqRepository(ApiEnvironmentType.SANDBOX, "test", saveSessionToFile)
        }
    }
}
