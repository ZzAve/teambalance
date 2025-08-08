package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.bank.BankConfig.BankBunqConfig
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment.PRODUCTION
import nl.jvandis.teambalance.api.bank.BankConfig.BunqEnvironment.SANDBOX
import nl.jvandis.teambalance.log
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Lazy
@Configuration
class BunqConfiguration(
    private val bankConfig: BankConfig,
) {
    @Bean
    fun setupBunqRepository(): BunqRepo {
        when (bankConfig.bunq.environment) {
            PRODUCTION -> initializeProductionSetup2(bankConfig.bunq)
            SANDBOX -> initializeSandboxSetup2(bankConfig.bunq)
        }
        return BunqRepo(bunqConfig = bankConfig.bunq)
    }

    private fun initializeProductionSetup2(bunqConfig: BankBunqConfig): BunqRepo {
        require(bunqConfig.environment == PRODUCTION) { "Bunq environment was not set to PRODUCTION" }
        require(bunqConfig.apiKey != null) { "No apikey was set for Bunq" }

        val obfuscatedApiKey = "${bunqConfig.apiKey.take(5)}******"
        log.info("Setting up connection with bunq PRODUCTION using api-key '$obfuscatedApiKey'")

        return try {
            BunqRepo(bunqConfig)
        } catch (t: Throwable) {
            throw IllegalStateException(
                "Could not create bunqRepository for production setup (apiKey: $obfuscatedApiKey, accountId: ${bunqConfig.bankAccountId})",
                t,
            )
        }
    }

    private fun initializeSandboxSetup2(bunqConfig: BankBunqConfig): BunqRepo {
        require(bunqConfig.environment == SANDBOX) {
            "Bunq environment was not set to PRODUCTION"
        }
        require(bunqConfig.apiKey.isNullOrEmpty() || bunqConfig.apiKey.startsWith("sandbox")) {
            """
            An apikey was set for Bunq while trying to setup SANDBOX environment. \
            This is not allowed, for your protection\
            """
        }

        log.info("Setting up connection with bunq SANDBOX")

        return try {
            BunqRepo(bunqConfig)
        } catch (t: Throwable) {
            throw IllegalStateException("Could not create bunqRepository for sandbox setup", t)
        }
    }
}
