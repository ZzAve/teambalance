package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.data.NO_ID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

data class Users(
    val users: List<User>
)

@Entity(name = "Uzer")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = NO_ID,

    @Column(nullable = false, unique = true) val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,

    @Column(nullable = false) val isActive: Boolean = true,
    @Column(nullable = true) val jerseyNumber: Int? = null,
    @Column(nullable = false) val showForTrainings: Boolean = true,
    @Column(nullable = false) val showForMatches: Boolean = true
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
    OTHER
}

//FIXME: You don't need me?
data class JooqUser(
    val id: Long = NO_ID,
    val name: String,
    val role: Role,
    val isActive: Boolean = true,
    val jerseyNumber: Int? = null,
    val showForTrainings: Boolean = true,
    val showForMatches: Boolean = true
)
