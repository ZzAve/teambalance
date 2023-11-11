package nl.jvandis.teambalance.api.bank

import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRANSACTION_EXCLUSION
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Repository

@Repository
class BankAccountTransactionExclusionRepository(private val context: MultiTenantDslContext) {
    fun findAll(): List<TransactionExclusion> {
        return context.select()
            .from(TRANSACTION_EXCLUSION)
            .fetch()
            .into(TransactionExclusion::class.java)
    }

    fun findByIdOrNull(transactionExclusionId: Long): TransactionExclusion? =
        context.select()
            .from(TRANSACTION_EXCLUSION)
            .where(TRANSACTION_EXCLUSION.ID.eq(transactionExclusionId))
            .fetch()
            .into(TransactionExclusion::class.java)
            .also {
                check(it.size < 2) { "Fetched more than 1 bankAccountAliases with the same id. Should not be possible!" }
            }
            .firstOrNull()

    fun insertMany(transactionExclusions: List<TransactionExclusion>): List<TransactionExclusion> {
        if (transactionExclusions.isEmpty()) {
            return emptyList()
        }
        val transactionExclusionsResult =
            context.insertInto(
                TRANSACTION_EXCLUSION,
                TRANSACTION_EXCLUSION.DATE,
                TRANSACTION_EXCLUSION.TRANSACTION_ID,
                TRANSACTION_EXCLUSION.COUNTER_PARTY,
                TRANSACTION_EXCLUSION.DESCRIPTION,
            )
                .valuesFrom(
                    transactionExclusions,
                    { it.date },
                    { it.transactionId },
                    { it.counterParty },
                    { it.description },
                )
                .returningResult(TRANSACTION_EXCLUSION.fields().toList())
                .fetch()
                .into(TransactionExclusion::class.java)

        return if (transactionExclusionsResult.size == transactionExclusions.size) {
            transactionExclusionsResult
        } else {
            throw DataAccessException("Could not insert transactionExclusions $transactionExclusions")
        }
    }

    fun insert(transactionExclusion: TransactionExclusion): TransactionExclusion {
        return insertMany(listOf(transactionExclusion)).first()
    }

    fun deleteById(transactionExclusionId: Long) {
        if (transactionExclusionId == NO_ID) {
            throw IllegalStateException(
                "TransactionExclusion with 'special' id $NO_ID can not be deleted. " +
                    "The special no id serves a special purpose in transforming items " +
                    "from records to entities and back",
            )
        }
        val execute =
            context.deleteFrom(TRANSACTION_EXCLUSION)
                .where(TRANSACTION_EXCLUSION.ID.eq(transactionExclusionId))
                .execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute bankAccountAliases, expected to remove only 1")
        }
    }
}
