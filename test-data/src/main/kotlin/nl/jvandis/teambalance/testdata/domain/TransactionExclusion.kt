package nl.jvandis.teambalance.testdata.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TransactionExclusions(
    val transactionExclusions: List<TransactionExclusion>
)
@Serializable
data class TransactionExclusion(
    val id: String,
    val date: LocalDate?,
    val transactionId: Int?,
    val counterParty: String?,
    val description: String?,
)

@Serializable
data class CreateTransactionExclusion(
    val date: LocalDate?,
    val transactionId: Int?,
    val counterParty: String?,
    val description: String?,
)
