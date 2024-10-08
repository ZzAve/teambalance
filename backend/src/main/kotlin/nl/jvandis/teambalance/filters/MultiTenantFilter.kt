package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.jvandis.teambalance.MultiTenantContext
import nl.jvandis.teambalance.Tenant
import nl.jvandis.teambalance.log
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@ConfigurationProperties("app.multi-tenancy")
data class TenantsConfig(
    val tenants: List<TenantConfig>,
) {
    init {
        val domains = tenants.map { it.domain }
        check(domains.size == domains.toSet().size) {
            "Tenants domain mapping is not unique. Pleas verify a domain is only used once. $tenants"
        }
    }

    data class TenantConfig(
        val domain: String,
        val tenant: Tenant,
        val bunqMeBaseUrl: String,
        val secret: Secret,
        val title: String,
    ) {
        /**
         * Specific constructor for configuration binding by spring. Highly unideal,
         * and deliberately had to put 'tenant' on top, to avoid JVM signature clashes between the constructors
         */
        @ConstructorBinding
        constructor(
            tenant: Tenant,
            domain: String,
            bunqMeBaseUrl: String,
            secret: String,
            title: String,
        ) : this(domain, tenant, bunqMeBaseUrl, Secret(secret), title)
    }

    @JvmInline
    value class Secret(val value: String) {
        override fun toString() = "Secret[***]"
    }
}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MultiTenantFilter(
    private val tenantsConfig: TenantsConfig,
) : OncePerRequestFilter() {
    companion object {
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val host = request.getHeader("Host")
        val tenant =
            tenantsConfig.tenants
                .firstOrNull { it.domain == host }

        if (tenant == null) {
            log.warn("Received a request from an unknown host $host")
            response.sendError(401, "Unknown domain. $host doesn't have anything to do with teambalance it seems")
        } else {
            MultiTenantContext.setCurrentTenant(tenant.tenant)
            response.addHeader("X-Tenant", MultiTenantContext.getCurrentTenant().name.lowercase())
        }

        filterChain.doFilter(request, response)
        MultiTenantContext.clear()
    }
}
