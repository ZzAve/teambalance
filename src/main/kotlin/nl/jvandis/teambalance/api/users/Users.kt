package nl.jvandis.teambalance.api.users

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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @Column(nullable = false, unique = true) val name: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val role: Role,
    @Column(nullable = false) val isActive: Boolean = true,
    @Column(nullable = false) val showForTrainings: Boolean = true,
    @Column(nullable = false) val showForMatches: Boolean = true
) {

    constructor(name: String, role: Role) : this(0, name, role)

    override fun toString() = "User[id=$id, name=$name, role=$role, isActive=$isActive, showForTrainings=$showForTrainings, showForMatches=$showForMatches]"
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
