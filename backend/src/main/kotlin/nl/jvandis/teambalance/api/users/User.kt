package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder

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

    data class Builder(
        val id: Long,
        val isActive: Boolean = true,
        val name: String,
        val role: Role,
        val showForTrainings: Boolean = true,
        val showForMatches: Boolean = true,
        val jerseyNumber: Int? = null,
        val teamBalanceId: String,
    ) : TeamBalanceEntityBuilder<User> {
        override fun build(): User =
            User(
                id = id,
                teamBalanceId = TeamBalanceId(teamBalanceId),
                name = name,
                role = role,
                isActive = isActive,
                jerseyNumber = jerseyNumber,
                showForTrainings = showForTrainings,
                showForMatches = showForMatches,
            )
    }
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
