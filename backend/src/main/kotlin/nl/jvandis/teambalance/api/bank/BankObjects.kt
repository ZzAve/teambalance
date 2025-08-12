package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import java.time.ZonedDateTime

data class BalanceResponse(
    val balance: String,
)

data class Transactions(
    val transactions: List<TransactionWithAlias>,
    val limit: Int,
)

data class TransactionWithAlias(
    val transaction: Transaction,
    val alias: User?,
)

data class Transaction(
    val id: String,
    val type: TransactionType,
    val currency: String,
    val amount: String,
    val counterParty: CounterParty,
//    val user: User?,
    val date: ZonedDateTime,
    val description: String?,
)

data class CounterParty(
    val iban: String?,
    val displayName: String,
)

enum class TransactionType {
    DEBIT,
    CREDIT,
}

data class TransactionsResponse(
    val transactions: List<TransactionResponse>,
)

data class TransactionResponse(
    val id: String,
    val type: TransactionType,
    val amount: String,
    val counterParty: String,
    val timestamp: Long,
)

data class Potters(
    val potters: List<Potter>,
    val amountOfTransactions: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val currency: String,
)

data class Potter(
    val name: String,
    val role: Role,
    val transactions: List<TransactionWithAlias>,
)

data class PottersResponse(
    val toppers: List<PotterResponse>,
    val floppers: List<PotterResponse>,
    val amountOfConsideredTransactions: Int,
    val from: ZonedDateTime,
    val until: ZonedDateTime,
    val subPeriod: PottersResponse?,
)

data class PotterResponse(
    val name: String,
    val role: Role,
    val currency: String,
    val amount: Double,
)

data class BankAccount(
    val id: Long,
    val balance: String,
    val alias: String?,
)
