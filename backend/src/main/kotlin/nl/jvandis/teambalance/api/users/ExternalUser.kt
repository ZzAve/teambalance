package nl.jvandis.teambalance.api.users

data class ExternalUsers(
    val users: List<ExternalUser>,
)

data class ExternalUser(
    val id: String,
    val name: String,
    val role: Role,
    val isActive: Boolean,
    val jerseyNumber: Int?,
)

fun Users.expose() = ExternalUsers(users.map(User::expose))

fun User.expose() =
    ExternalUser(
        id = teamBalanceId.value,
        name = name,
        role = role,
        jerseyNumber = jerseyNumber,
        isActive = isActive,
    )
