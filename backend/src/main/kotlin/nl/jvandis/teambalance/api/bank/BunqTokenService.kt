package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.ConfigurationRepository
import nl.jvandis.teambalance.log
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for storing and retrieving Bunq access tokens.
 */
@Service
class BunqTokenService(
    private val configurationRepository: ConfigurationRepository,
    private val encryptionService: BunqTokenEncryptionService,
) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "bunq-access-token"
    }

    /**
     * Gets the encrypted access token from the database.
     *
     * @return The decrypted access token, or null if not found.
     */
    fun getAccessToken(): String? {
        return configurationRepository.getConfig(ACCESS_TOKEN_KEY)?.let { encryptedToken ->
            try {
                encryptionService.decrypt(encryptedToken)
            } catch (e: Exception) {
                log.error("Error decrypting access token", e)
                null
            }
        }
    }

    /**
     * Saves the access token to the database.
     *
     * @param accessToken The access token to save.
     * @return True if the token was saved successfully, false otherwise.
     */
    @Transactional
    fun saveAccessToken(accessToken: String) {
        val encryptedToken = encryptionService.encrypt(accessToken)
        return configurationRepository.setConfig(ACCESS_TOKEN_KEY, encryptedToken)
    }


    /**
     * Clears all token-related data from the database.
     *
     * @return True if all data was cleared successfully, false otherwise.
     */
    @Transactional
    fun clearToken(): Boolean {
        return configurationRepository.deleteConfig(ACCESS_TOKEN_KEY)
    }
}
