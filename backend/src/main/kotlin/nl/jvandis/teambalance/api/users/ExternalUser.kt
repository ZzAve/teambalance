package nl.jvandis.teambalance.api.users

data class ExternalUsers(
    val users: List<ExternalUser>,
)

data class ExternalUser(
    val id: Long,
    val name: String,
    val role: Role,
    val isActive: Boolean,
    val jerseyNumber: Int?,
)

fun Users.toResponse() = expose()

fun Users.expose() = ExternalUsers(users.map(User::expose))

fun User.toResponse() = expose()

fun User.expose() =
    ExternalUser(
        id = id,
        name = name,
        role = role,
        jerseyNumber = jerseyNumber,
        isActive = isActive,
    )
