package nl.jvandis.teambalance.api.attendees

import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.bank.BankAccountAlias
import nl.jvandis.teambalance.api.event.TeamBalanceRecordHandler
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.jooq.schema.tables.records.BankAccountAliasRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.BANK_ACCOUNT_ALIAS
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.Record

class BankAccountAliasWithUserRecordHandler : TeamBalanceRecordHandler<BankAccountAlias> {
    private val bankAccountAliasRecords = mutableMapOf<Long, BankAccountAliasRecord>()
    private val userRecords = mutableMapOf<Long, UzerRecord>()

    private var recordsHandled = 0L
    private var result: List<BankAccountAlias>? = null

    override fun accept(record: Record) {
        recordsHandled++
        val bankAccountAliasId = record[BANK_ACCOUNT_ALIAS.ID]
        val userId = record[UZER.ID]
        if (bankAccountAliasId == null || userId == null) {
            return
        }

        bankAccountAliasRecords.computeIfAbsent(bankAccountAliasId) {
            // mapping via AttendeeRecords works better with column name clashes (like `id`)
            record.into(BankAccountAliasRecord::class.java)
        }

        userRecords.computeIfAbsent(userId) {
            record.into(UzerRecord::class.java)
        }
    }

    fun stats(): String {
        return """
            Nr of records handled: $recordsHandled. 
            Nr of bankAccountAliases Created: ${bankAccountAliasRecords.size}. 
            Nr of users created: ${userRecords.size}"
            """.trimIndent()
    }

    override fun build(): List<BankAccountAlias> {
        return result ?: run {
            val buildResult =
                bankAccountAliasRecords.values.map {
                    it.toBankAccountAlias(userRecords[it.userId!!]!!) // FIXME
                }
            result = buildResult
            buildResult
        }
    }
}

fun BankAccountAliasRecord.toBankAccountAlias(uzerRecord: UzerRecord): BankAccountAlias {
    check(this.userId == uzerRecord.id) { "userId does not match uzerRecord id" }
    return BankAccountAlias(
        id = getFieldOrThrow(BANK_ACCOUNT_ALIAS.ID),
        teamBalanceId = TeamBalanceId(getFieldOrThrow(BANK_ACCOUNT_ALIAS.TEAM_BALANCE_ID)),
        alias = getFieldOrThrow(BANK_ACCOUNT_ALIAS.ALIAS),
        user = uzerRecord.into(User::class.java),
    )
}
