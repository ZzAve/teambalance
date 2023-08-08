package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.data.NO_ID

data class Users(
    val users: List<User>
)

data class User(
    val id: Long,
    val name: String,
    val role: Role,
    val isActive: Boolean = true,
    val jerseyNumber: Int? = null,
    val showForTrainings: Boolean = true,
    val showForMatches: Boolean = true
) {
    constructor(name: String, role: Role) : this(NO_ID, name, role)
}

enum class Role {
    TRAINER,
    COACH,
    SETTER,
    MID,
    DIAGONAL,
    PASSER,
    LIBERO,
    OTHER
}
