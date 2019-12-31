package nl.jvandis.teambalance.api.bank

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime


data class BalanceResponse(
        val balance: String
)

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
        val transactions: List<TransactionResponse>
)

data class TransactionResponse(
        val id: Int,
        val amount: String,
        val counterParty: String,
        val timestamp: Long
)

data class Error(
        val status: HttpStatus,
        val reason: String
)
