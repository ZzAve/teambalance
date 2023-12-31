package nl.jvandis.teambalance

import org.slf4j.MDC

enum class Tenant {
    TOVO_HEREN_4,
    TOVO_HEREN_5,
}

object MultiTenantContext {
    private val currentTenant: ThreadLocal<Tenant> = InheritableThreadLocal()

    fun getCurrentTenant(): Tenant = currentTenant.get() ?: throw IllegalArgumentException("Unknown tenant")

    fun setCurrentTenant(tenant: Tenant) {
        currentTenant.set(tenant)
        MDC.put("tenant", tenant.name)
    }

    fun clear() {
        currentTenant.set(null)
        MDC.remove("tenant")
    }
}
