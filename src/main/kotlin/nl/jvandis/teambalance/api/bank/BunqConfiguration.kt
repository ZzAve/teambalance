package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.context.ApiEnvironmentType
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Configuration
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Factory
class BunqConfiguration(
    @Value("\${app.bank.api-key}") private val apiKey: String,
    @Value("\${app.bank.saveSessionToFile:false}") private val saveSessionToFile: Boolean
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun bunqRepository(): BunqRepository {
        log.info("Using api-key '${apiKey.substring(0, 5)}******${apiKey.substring(apiKey.length - 5)}'")
        return try {
            BunqRepository(ApiEnvironmentType.PRODUCTION, apiKey, saveSessionToFile)
        } catch (t: Throwable) {
            log.error("Could not create BunqRepo:", t)
            BunqRepository(ApiEnvironmentType.SANDBOX, "test", saveSessionToFile)
        }
    }
}
