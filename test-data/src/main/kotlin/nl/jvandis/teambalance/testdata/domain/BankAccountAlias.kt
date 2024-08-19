package nl.jvandis.teambalance.testdata.domain

import kotlinx.serialization.Serializable

@Serializable
data class BankAccountAliases(
    val bankAccountAliases: List<BankAccountAlias>
)

@Serializable
data class BankAccountAlias(
    val id: String,
    val alias: String,
    val user: User,
)


@Serializable
data class CreateBankAccountAlias(
    val alias: String,
    val userId: String,
)


