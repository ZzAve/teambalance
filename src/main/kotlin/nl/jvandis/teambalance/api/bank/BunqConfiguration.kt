package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.context.ApiEnvironmentType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BunqConfiguration(
        @Value("\${app.bank.api-key}") private val apiKey: String,
        @Value("\${app.bank.saveSessionToFile:false}") private val saveSessionToFile: Boolean
) {
    val log = LoggerFactory.getLogger(javaClass)
    @Bean
    fun bunqLib(): BunqRepository {
        log.info("Using api-key '${apiKey.substring(0, 5)}******${apiKey.substring(apiKey.length - 5)}'")
        return try {
            BunqRepository(ApiEnvironmentType.PRODUCTION, apiKey, saveSessionToFile)
        } catch (t: Throwable) {
            log.error("Could not create BunqRepo:", t)
            BunqRepository(ApiEnvironmentType.SANDBOX, "test", saveSessionToFile)
        }
    }
}