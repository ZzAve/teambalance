package nl.jvandis.teambalance.api

import io.micronaut.context.annotation.Value
import org.slf4j.LoggerFactory
import java.util.Base64
import javax.inject.Singleton

const val SECRET_HEADER = "X-Secret"

@Singleton
class SecretService(
    @Value("\${app.bank.secret-value}") private val validSecretValue: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun ensureSecret(secret: String?) {
        val decodedSecret = decodeSecret(secret)
        if (decodedSecret != validSecretValue) {
            throw InvalidSecretException("There was no secret provided, or value was not valid")
        }
    }

    private fun decodeSecret(secret: String?): String? = secret?.let {
        try {
            val decoded = Base64.getDecoder().decode(it)
            String(decoded, Charsets.UTF_8)
        } catch (t: Throwable) {
            log.error("Could not parse secret because of encoding issue ($secret)", t)
            null
        }
    }
}
