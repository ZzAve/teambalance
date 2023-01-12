package nl.jvandis.teambalance.api.bank

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface BankAccountAliasRepository : CrudRepository<BankAccountAlias, Long> {

    @Query("SELECT b,u from BankAccountAlias b LEFT JOIN Uzer u on b.user.id = u.id")
    override fun findAll(): List<BankAccountAlias>
}
