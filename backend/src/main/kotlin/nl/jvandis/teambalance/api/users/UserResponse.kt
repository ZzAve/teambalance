package nl.jvandis.teambalance.api.users

data class UserResponses(
    val users: List<UserResponse>,
)

data class UserResponse(
    val id: String,
    val name: String,
    val role: Role,
    val isActive: Boolean,
    val jerseyNumber: Int?,
)

fun Users.expose() = UserResponses(users.map(User::expose))

fun User.expose() =
    UserResponse(
        id = teamBalanceId.value,
        name = name,
        role = role,
        jerseyNumber = jerseyNumber,
        isActive = isActive,
    )
