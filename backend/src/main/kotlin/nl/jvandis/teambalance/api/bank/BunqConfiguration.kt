package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.bank.BunqEnvironment.PRODUCTION
import nl.jvandis.teambalance.api.bank.BunqEnvironment.SANDBOX
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
        return when (bankConfig.bunq.environment) {
            PRODUCTION -> initializeProductionSetup(bankConfig.bunq)
            SANDBOX -> initializeSandboxSetup(bankConfig.bunq)
        }
    }

    private fun initializeProductionSetup(bunqConfig: BankBunqConfig): BunqRepository {
        require(bunqConfig.environment == PRODUCTION) { "Bunq environment was not set to PRODUCTION" }
        require(bunqConfig.apiKey != null) { "No apikey was set for Bunq" }
        require(bunqConfig.bankAccountId != null) { "No bankAccountId was set for Bunq" }

        val obfuscatedApiKey = "${bunqConfig.apiKey.take(5)}******"
        log.info("Setting up connection with bunq PRODUCTION using api-key '$obfuscatedApiKey'")

        return try {
            BunqRepository(bunqConfig)
        } catch (t: Throwable) {
            throw IllegalStateException(
                "Could not create bunqRepository for production setup (apiKey: $obfuscatedApiKey, accountId: ${bunqConfig.bankAccountId})",
                t
            )
        }
    }

    private fun initializeSandboxSetup(bunqConfig: BankBunqConfig): BunqRepository {
        require(bunqConfig.environment == SANDBOX) {
            "Bunq environment was not set to PRODUCTION"
        }
        require(bunqConfig.apiKey.isNullOrEmpty()) {
            "An apikey was set for Bunq while trying to setup SANDBOX environment. " +
                "This is not allowed, for your protection"
        }
        require(bunqConfig.bankAccountId == null || bunqConfig.bankAccountId == -1) {
            "A bankAccountId was set while trying to setup SANDBOX environment. " +
                "This is not allowed, for your protection"
        }
        log.info("Setting up connection with bunq SANDBOX")

        return try {
            BunqRepository(bunqConfig)
        } catch (t: Throwable) {
            throw IllegalStateException("Could not create bunqRepository for sandbox setup", t)
        }
    }
}
