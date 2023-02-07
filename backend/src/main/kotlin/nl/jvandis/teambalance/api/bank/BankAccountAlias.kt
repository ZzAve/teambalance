package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID

data class BankAccountAliases(
    val bankAccountAliases: List<BankAccountAlias>
)

data class BankAccountAlias(
    val id: Long,
    val alias: String,
    val user: User
) {
    constructor(alias: String, user: User) : this(NO_ID, alias, user)
}
