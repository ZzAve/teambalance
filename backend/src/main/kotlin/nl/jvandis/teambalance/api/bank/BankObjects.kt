package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.User
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
    val type: TransactionType,
    val currency: String,
    val amount: String,
    val counterParty: String,
    val user: User?,
    val date: ZonedDateTime
)

enum class TransactionType {
    DEBIT,
    CREDIT
}

data class TransactionsResponse(
    val transactions: List<TransactionResponse>
)

data class TransactionResponse(
    val id: Int,
    val type: TransactionType,
    val amount: String,
    val counterParty: String,
    val timestamp: Long
)

data class Potters(
    val potters: List<Potter>,
    val amountOfTransactions: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val currency: String
)

data class Potter(
    val name: String,
    val transactions: List<Transaction>
)

data class PottersResponse(
    val toppers: List<PotterResponse>,
    val floppers: List<PotterResponse>,
    val amountOfConsideredTransactions: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val subPeriod: PottersResponse?
)

data class PotterResponse(
    val name: String,
    val currency: String,
    val amount: Double
)
