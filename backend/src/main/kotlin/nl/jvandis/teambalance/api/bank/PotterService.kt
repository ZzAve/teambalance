package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class PotterService(
    private val bankService: BankService,
    private val userRepository: UserRepository,
) {
    fun getPotters(
        since: ZonedDateTime,
        includeInactiveUsers: Boolean,
        includeSupportRoles: Boolean
    ): Potters {
        val relevantTransactions = getRelevantTransactions(since, includeSupportRoles)
        val groupedTransactions: Map<User, List<Transaction>> =
            relevantTransactions
                .groupBy { x -> x.user!! }

        val potters: List<Potter> =
            userRepository.findAll()
                .filter { includeSupportRoles || !SUPPORT_TEAM_ROLES.contains(it.role) }
                .filter { includeInactiveUsers || it.isActive }
                .map {
                    val transactions = groupedTransactions[it] ?: emptyList()
                    Potter(it.name, transactions)
                }

        return Potters(
            potters = potters,
            currency = "€",
            amountOfTransactions = relevantTransactions.size,
            from = since,
            until = if (relevantTransactions.isNotEmpty()) relevantTransactions.last().date else since,
        )
    }

    private fun getRelevantTransactions(since: ZonedDateTime, includeSupportRoles: Boolean) =
        bankService.getTransactions().transactions
            .asSequence()
            .filter { it.date > since }
            .filter { it.type == TransactionType.DEBIT && it.currency == "€" }
            .filter { it.user != null }
            .filter { includeSupportRoles || !SUPPORT_TEAM_ROLES.contains(it.user?.role) }
            .toList()

    companion object {
        private val SUPPORT_TEAM_ROLES = setOf(Role.COACH, Role.TRAINER, Role.OTHER)
    }
}
