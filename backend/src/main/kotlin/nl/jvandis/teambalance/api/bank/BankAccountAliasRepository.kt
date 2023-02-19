package nl.jvandis.teambalance.api.bank

import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.attendees.BankAccountAliasWithUserRecordHandler
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.references.BANK_ACCOUNT_ALIAS
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class BankAccountAliasRepository(private val context: DSLContext) {
    fun findAll(): List<BankAccountAlias> {
        val recordHandler = BankAccountAliasWithUserRecordHandler()
        context.select()
            .from(BANK_ACCOUNT_ALIAS)
            .leftJoin(UZER)
            .on(BANK_ACCOUNT_ALIAS.USER_ID.eq(UZER.ID))
            .fetch().forEach(recordHandler)
        return recordHandler.build()
    }

    fun findByIdOrNull(aliasId: Long): BankAccountAlias? {

        val recordHandler = BankAccountAliasWithUserRecordHandler()
        context.select()
            .from(BANK_ACCOUNT_ALIAS)
            .leftJoin(UZER)
            .on(BANK_ACCOUNT_ALIAS.USER_ID.eq(UZER.ID))
            .where(BANK_ACCOUNT_ALIAS.ID.eq(aliasId))
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
        val insertResult = context.insertInto(
            BANK_ACCOUNT_ALIAS,
            BANK_ACCOUNT_ALIAS.USER_ID,
            BANK_ACCOUNT_ALIAS.ALIAS
        )
            .valuesFrom(
                aliases,
                { it.user.id },
                { it.alias }
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

    fun deleteById(bankAccountAliasId: Long) {
        if (bankAccountAliasId == NO_ID) {
            throw IllegalStateException(
                "User with 'special' id $NO_ID can not be deleted. " +
                        "The special no id serves a special purpose in transforming items " +
                        "from records to entities and back"
            )
        }
        val execute = context.deleteFrom(BANK_ACCOUNT_ALIAS)
            .where(BANK_ACCOUNT_ALIAS.ID.eq(bankAccountAliasId))
            .execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute bankAccountAliases, expected to remove only 1")
        }
    }
}
