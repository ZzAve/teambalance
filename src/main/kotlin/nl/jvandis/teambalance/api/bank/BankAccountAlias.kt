package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.api.users.User
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint


data class BankAccountAliases(
    val bankAccountAliases: List<BankAccountAlias>
)

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("alias"))))
data class BankAccountAlias(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @Column(nullable = false, name = "alias") val alias: String,
    @ManyToOne @JoinColumn(name = "USER_ID") val user: User
    ){

    constructor(alias: String, user: User) : this(0,alias,user)
}
