package nl.jvandis.teambalance.api.training

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


data class Users(
        val users: List<User>
)

@Entity
data class User(
        @GeneratedValue
        @Id val id: Long,
        @Column(nullable = false) val name: String,
        @Column(nullable = false) val role: Role
) {

    constructor(name: String, role: Role) : this(0, name, role)


    override fun toString() = "User[id=$id, name=$name, role=$role]"

}


enum class Role {
    TRAINER,
    COACH,
    SETTER,
    MID,
    DIAGONAL,
    PASSER
}
