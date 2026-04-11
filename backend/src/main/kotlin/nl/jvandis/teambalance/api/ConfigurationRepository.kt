package nl.jvandis.teambalance.api

import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.CONFIG
import org.springframework.stereotype.Repository

@Repository
class ConfigurationRepository(
    private val context: MultiTenantDslContext,
) {
    fun getConfig(key: String): String? =
        context
            .select(CONFIG.VALUE)
            .from(CONFIG)
            .where(CONFIG.KEY.eq(key))
            .fetch()
            .into(String::class.java)
            .firstOrNull()

    fun upsertConfig(
        key: String,
        value: String,
    ) {
        context
            .insertInto(CONFIG, CONFIG.KEY, CONFIG.VALUE)
            .values(key, value)
            .onConflict(CONFIG.KEY)
            .doUpdate()
            .set(CONFIG.VALUE, value)
            .execute()
    }
}
