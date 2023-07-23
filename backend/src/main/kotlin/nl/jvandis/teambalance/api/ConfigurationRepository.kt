package nl.jvandis.teambalance.api

import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.CONFIG
import org.springframework.stereotype.Repository

@Repository
class ConfigurationRepository(private val context: MultiTenantDslContext) {

    fun getConfig(key: String): String? {
        return context.select(CONFIG.VALUE)
            .from(CONFIG)
            .where(CONFIG.KEY.eq(key))
            .fetch()
            .into(String::class.java)
            .firstOrNull()
    }

}
