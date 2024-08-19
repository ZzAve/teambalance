package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.UserResponse
import nl.jvandis.teambalance.api.users.expose

data class BankAccountAliasesResponse(
    val bankAccountAliases: List<BankAccountAliasResponse>
)

data class BankAccountAliasResponse(
    val id: String,
    val alias: String,
    val user: UserResponse,
)

fun BankAccountAliases.expose() = BankAccountAliasesResponse(bankAccountAliases.map(BankAccountAlias::expose))

fun BankAccountAlias.expose() = BankAccountAliasResponse(
    id = teamBalanceId.value,
    alias = alias,
    user = user.expose()
)
