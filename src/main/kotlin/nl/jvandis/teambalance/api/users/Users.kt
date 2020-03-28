package nl.jvandis.teambalance.api.users

import javax.persistence.*


data class Users(
        val users: List<User>
)

@Entity(name="Uzer")
data class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
        @Column(nullable = false, unique = true) val name: String,
        @Enumerated(EnumType.STRING) @Column(nullable = false) val role: Role
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