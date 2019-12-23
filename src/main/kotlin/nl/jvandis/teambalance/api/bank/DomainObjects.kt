package nl.jvandis.teambalance.api.bank

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class Transactions(
        val transactions: List<Transaction>,
        val limit: Int
)

data class Transaction(
        val id: Int,
        val amount: String,
        val counterParty: String,
        val date: ZonedDateTime
)

data class TransactionsResponse(
        val transactions: List<Transaction>
)

data class Error(
        val status: HttpStatus,
        val reason: String
)
