package nl.jvandis.teambalance.api.bank

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@ConfigurationProperties("app.encryption")
data class EncryptionConfig(val encryptionKey: String)

/**
 * Service for encrypting and decrypting Bunq access tokens using AES-GCM.
 */
@Service
class BunqTokenEncryptionService(
    encryptionConfig: EncryptionConfig
) {
    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128
        private const val IV_LENGTH_BYTE = 12
    }

    private final val secretKeyBytes = Base64.getDecoder().decode(encryptionConfig.encryptionKey)
    private final val secretKey = SecretKeySpec(secretKeyBytes, ALGORITHM)

    /**
     * Encrypts the given access token using AES-GCM.
     *
     * @param accessToken The access token to encrypt.
     * @return The encrypted access token as a Base64-encoded string.
     */
    fun encrypt(accessToken: String): String {
        // Generate a random IV (Initialization Vector)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)

        // Create GCM parameter specification
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)

        // Initialize cipher for encryption
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        // Encrypt the access token
        val encryptedBytes = cipher.doFinal(accessToken.toByteArray())

        // Combine IV and encrypted bytes
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        // Encode as Base64 string
        return Base64.getEncoder().encodeToString(combined)
    }

    /**
     * Decrypts the given encrypted access token.
     *
     * @param encryptedAccessToken The encrypted access token as a Base64-encoded string.
     * @return The decrypted access token.
     */
    fun decrypt(encryptedAccessToken: String): String {
        // Decode from Base64
        val combined = Base64.getDecoder().decode(encryptedAccessToken)

        // Extract IV and encrypted bytes
        val iv = ByteArray(IV_LENGTH_BYTE)
        val encryptedBytes = ByteArray(combined.size - IV_LENGTH_BYTE)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)

        // Create GCM parameter specification
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)

        // Initialize cipher for decryption
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        // Decrypt the access token
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        // Convert to string and return
        return String(decryptedBytes)
    }
}
