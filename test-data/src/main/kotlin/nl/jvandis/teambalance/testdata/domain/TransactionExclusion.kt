package nl.jvandis.teambalance.testdata.domain

import java.time.LocalDate

data class TransactionExclusion(
    val id: String,
    val date: LocalDate?,
    val transactionId: Int?,
    val counterParty: String?,
    val description: String?,
)
