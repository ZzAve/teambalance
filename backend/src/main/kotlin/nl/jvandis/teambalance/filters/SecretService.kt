package nl.jvandis.teambalance.filters

import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.api.InvalidSecretException
import nl.jvandis.teambalance.loggerFor
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class SecretService(
    private val tenantsConfig: TenantsConfig
) {
    private val log = loggerFor()

    fun ensureSecret(secret: String?) {
        val decodedSecret = decodeSecret(secret)
        val validSecretValue = tenantsConfig.tenants.first { it.tenant == MultiTenantContext.getCurrentTenant() }.secret

        if (decodedSecret != validSecretValue.value) {
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
