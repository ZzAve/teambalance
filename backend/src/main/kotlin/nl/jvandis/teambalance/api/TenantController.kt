package nl.jvandis.teambalance.api

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.filters.TenantsConfig
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "tenants")
@RequestMapping(path = ["/api/tenants"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TenantController(
    private val tenantsConfig: TenantsConfig,
) {
    @GetMapping("/me")
    fun getCurrentTenant(): TenantResponse {
        val currentTenant = MultiTenantContext.getCurrentTenant()
        return tenantsConfig.tenants
            .first { it.tenant == currentTenant }
            .expose()
    }

    private fun TenantsConfig.TenantConfig.expose() = TenantResponse(domain, title, bunqMeBaseUrl)
}

data class TenantResponse(
    val domain: String,
    val title: String,
    val bunqMeBaseUrl: String,
)
