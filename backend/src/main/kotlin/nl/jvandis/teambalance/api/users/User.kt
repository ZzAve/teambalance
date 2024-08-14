package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.NO_ID

data class Users(
    val users: List<User>,
)

data class User(
    val id: Long,
    val teamBalanceId: TeamBalanceId,
    val name: String,
    val role: Role,
    val isActive: Boolean = true,
    val jerseyNumber: Int? = null,
    val showForTrainings: Boolean = true,
    val showForMatches: Boolean = true,
) {
    constructor(name: String, role: Role) : this(
        id = NO_ID,
        name = name,
        role = role,
        teamBalanceId = TeamBalanceId.random(),
    )
}

enum class Role {
    TRAINER,
    COACH,
    SETTER,
    MID,
    DIAGONAL,
    PASSER,
    LIBERO,
    OTHER,
}
