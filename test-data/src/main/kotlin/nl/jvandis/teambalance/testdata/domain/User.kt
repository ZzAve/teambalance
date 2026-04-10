package nl.jvandis.teambalance.testdata.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val name: String,
    val role: Role,
)

@Serializable
data class Users(
    val users: List<User>,
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val role: Role,
    @SerialName("active") val isActive: Boolean,
    val jerseyNumber: Int? = null,
)

@Serializable
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

@Serializable
data class PotentialUserUpdate(
    val name: String?,
    val role: Role?,
    @SerialName("active") val isActive: Boolean?,
    val jerseyNumber: Int?,
)
