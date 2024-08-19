package nl.jvandis.teambalance.api.bank

import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.attendees.BankAccountAliasWithUserRecordHandler
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.BANK_ACCOUNT_ALIAS
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.Record1
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class BankAccountAliasRepository(private val context: MultiTenantDslContext) {
    fun findAll(): List<BankAccountAlias> {
        val recordHandler = BankAccountAliasWithUserRecordHandler()
        context.select()
            .from(BANK_ACCOUNT_ALIAS)
            .leftJoin(UZER)
            .on(BANK_ACCOUNT_ALIAS.USER_ID.eq(UZER.ID))
            .fetch().forEach(recordHandler)
        return recordHandler.build()
    }

    fun findByIdOrNull(aliasId: TeamBalanceId): BankAccountAlias? {
        val recordHandler = BankAccountAliasWithUserRecordHandler()
        context.select()
            .from(BANK_ACCOUNT_ALIAS)
            .leftJoin(UZER)
            .on(BANK_ACCOUNT_ALIAS.USER_ID.eq(UZER.ID))
            .where(BANK_ACCOUNT_ALIAS.TEAM_BALANCE_ID.eq(aliasId.value))
            .fetch()
            .forEach(recordHandler)

        return recordHandler
            .build()
            .also {
                check(it.size < 2) { "Fetched more than 1 bankAccountAliases with the same id. Should not be possible!" }
            }
            .firstOrNull()
    }

    fun insertMany(aliases: List<BankAccountAlias>): List<BankAccountAlias> {
        if (aliases.isEmpty()) {
            return emptyList()
        }
        val insertResult =
            context.insertInto(
                BANK_ACCOUNT_ALIAS,
                BANK_ACCOUNT_ALIAS.TEAM_BALANCE_ID,
                BANK_ACCOUNT_ALIAS.USER_ID,
                BANK_ACCOUNT_ALIAS.ALIAS,
            )
                .valuesFrom(
                    aliases,
                    { it.teamBalanceId.value },
                    { it.user.id },
                    { it.alias },
                )
                .returningResult(BANK_ACCOUNT_ALIAS.ID).fetch()

        val recordHandler = BankAccountAliasWithUserRecordHandler()

        context.select()
            .from(BANK_ACCOUNT_ALIAS)
            .leftJoin(UZER)
            .on(UZER.ID.eq(BANK_ACCOUNT_ALIAS.USER_ID))
            .where(BANK_ACCOUNT_ALIAS.ID.`in`(insertResult.mapNotNull(Record1<Long?>::value1)))
            .fetch().forEach(recordHandler)

        return if (insertResult.size == aliases.size) {
            recordHandler.build()
        } else {
            throw DataAccessException("Could not insert aliases $aliases")
        }
    }

    fun insert(bankAccountAlias: BankAccountAlias): BankAccountAlias {
        return insertMany(listOf(bankAccountAlias)).first()
    }

    fun deleteById(bankAccountAliasId: TeamBalanceId) {
        val execute =
            context.deleteFrom(BANK_ACCOUNT_ALIAS)
                .where(BANK_ACCOUNT_ALIAS.TEAM_BALANCE_ID.eq(bankAccountAliasId.value))
                .execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute bankAccountAliases, expected to remove only 1")
        }
    }
}
