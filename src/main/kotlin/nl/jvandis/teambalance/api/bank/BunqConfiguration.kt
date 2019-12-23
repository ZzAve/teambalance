package nl.jvandis.teambalance.api.bank

import com.bunq.sdk.context.ApiEnvironmentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BunqConfiguration(
        @Value("\${app.bank.api-key}") private val apiKey: String
) {
    @Bean
    fun bunqLib(): BunqRepository {
        return BunqRepository(ApiEnvironmentType.PRODUCTION, apiKey)
    }
}