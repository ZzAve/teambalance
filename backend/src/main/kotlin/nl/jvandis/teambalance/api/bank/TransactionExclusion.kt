package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.data.NO_ID
import java.time.LocalDate

data class TransactionExclusions(
    val transactionExclusions: List<TransactionExclusion>,
)

data class TransactionExclusion(
    val id: Long = NO_ID,
    val date: LocalDate?,
    val transactionId: Int?,
    val counterParty: String?,
    val description: String?,
) {
    constructor(
        date: LocalDate? = null,
        transactionId: Int? = null,
        counterParty: String? = null,
        description: String? = null,
    ) : this(
        id = NO_ID,
        date = date,
        transactionId = transactionId,
        counterParty = counterParty,
        description = description,
    )
}
