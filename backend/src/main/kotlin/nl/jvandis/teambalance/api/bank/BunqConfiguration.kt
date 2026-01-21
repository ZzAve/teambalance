package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.bank.BunqEnvironment.PRODUCTION
import nl.jvandis.teambalance.api.bank.BunqEnvironment.SANDBOX
import nl.jvandis.teambalance.log
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Lazy
@Configuration
class BunqConfiguration(
    private val bankConfig: BankConfig,
    private val tokenService: BunqTokenService,
) {
    @Bean
    fun bunqLib(): BunqRepository {
        return when (bankConfig.bunq.environment) {
            PRODUCTION -> initializeProductionSetup(bankConfig.bunq)
            SANDBOX -> initializeSandboxSetup(bankConfig.bunq)
        }
    }

    private fun initializeProductionSetup(bunqConfig: BankBunqConfig): BunqRepository {
        require(bunqConfig.environment == PRODUCTION) { "Bunq environment was not set to PRODUCTION" }

        verifyAccessTokenMigration()

        // Get the access token from the token service
        val accessToken = tokenService.getAccessToken()

        // If no access token is available, use the API key from the configuration (if available)
        val apiKey = accessToken ?: bunqConfig.apiKey

        if (apiKey == null) {
            throw IllegalStateException("No Bunq connection available. Please connect with Bunq first.")
        }

        val obfuscatedApiKey = "${apiKey.take(5)}******"
        log.info("Setting up connection with bunq PRODUCTION using access token '$obfuscatedApiKey'")

        // Create a copy of the config with the access token as the API key
        val configWithAccessToken = bunqConfig.copy(apiKey = apiKey)

        return try {
            BunqRepository(configWithAccessToken)
        } catch (t: Throwable) {
            throw IllegalStateException(
                "Could not create bunqRepository for production setup (accessToken: $obfuscatedApiKey, accountId: ${bunqConfig.bankAccountId})",
                t,
            )
        }
    }

    private fun initializeSandboxSetup(bunqConfig: BankBunqConfig): BunqRepository {
        require(bunqConfig.environment == SANDBOX) {
            "Bunq environment was not set to PRODUCTION"
        }
        require(bunqConfig.apiKey.isNullOrEmpty()) {
            """
            An apikey was set for Bunq while trying to setup SANDBOX environment. \
            This is not allowed, for your protection\
            """
        }
//        require(bunqConfig.bankAccountId == null || bunqConfig.bankAccountId == -1) {
//            "A bankAccountId was set while trying to setup SANDBOX environment. " +
//                "This is not allowed, for your protection"
//        }
        log.info("Setting up connection with bunq SANDBOX")

        return try {
            BunqRepository(bunqConfig)
        } catch (t: Throwable) {
            throw IllegalStateException("Could not create bunqRepository for sandbox setup", t)
        }
    }

    fun verifyAccessTokenMigration() {
        // Check if an API key exists
        val apiKey = bankConfig.bunq.apiKey
        if (!apiKey.isNullOrBlank()) {
            // Check if an access token already exists
            val existingToken = tokenService.getAccessToken()
            if (existingToken != null) {
                log.warn("Both API key and access token exist. Not overwriting access token with API key.")
            } else {
                // Save the API key as an access token
                tokenService.saveAccessToken(apiKey)
                log.info("Migrated API key to access token.")
            }
        }
    }

}
