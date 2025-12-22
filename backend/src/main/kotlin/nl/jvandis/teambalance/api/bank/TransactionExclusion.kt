package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.NO_ID
import java.time.LocalDate

data class TransactionExclusions(
    val transactionExclusions: List<TransactionExclusion>,
)

/**
 * Represents an exclusion of a transaction from team balance calculations.
 *
 * Conceptually, a transaction exclusion represents a block of rules that should be ignored for a specific team balance.
 * This can be used to ignore transactions that are unrelated to the teambalance, or to course correct the potters / floppers
 */
data class TransactionExclusion(
    val id: Long = NO_ID,
    val teamBalanceId: TeamBalanceId,
    val date: LocalDate?,
    val transactionId: String?,
    val counterParty: String?,
    val description: String?,
) {
    constructor(
        date: LocalDate? = null,
        transactionId: String? = null,
        counterParty: String? = null,
        description: String? = null,
    ) : this(
        id = NO_ID,
        teamBalanceId = TeamBalanceId.random(),
        date = date,
        transactionId = transactionId,
        counterParty = counterParty,
        description = description,
    )
}
