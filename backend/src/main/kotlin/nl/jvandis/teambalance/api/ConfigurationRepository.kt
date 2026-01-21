package nl.jvandis.teambalance.api

import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.CONFIG
import org.jooq.Asterisk
import org.jooq.SelectFieldOrAsterisk
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

    fun setConfig(key: String, value: String) {
        val rowExists = context.select(CONFIG, CONFIG.KEY)
            .where(CONFIG.KEY.eq(key))
            .execute() > 0

        if (rowExists) {
            context.update(CONFIG)
                .set(CONFIG.VALUE, value)
                .where(CONFIG.KEY.eq(key))
                .execute()
        } else {
            context.insertInto(CONFIG, CONFIG.KEY, CONFIG.VALUE)
                .values(key, value)
                .execute()
        }
    }


        fun deleteConfig(accessTokenKey: String): Boolean {
            return context.deleteFrom(CONFIG)
                .where(CONFIG.KEY.eq(accessTokenKey))
                .execute() > 0
        }
}
