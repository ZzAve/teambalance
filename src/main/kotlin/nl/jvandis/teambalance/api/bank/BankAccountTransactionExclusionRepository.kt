package nl.jvandis.teambalance.api.bank

import org.springframework.data.repository.CrudRepository

interface BankAccountTransactionExclusionRepository : CrudRepository<TransactionExclusion, Long> {

    override fun findAll(): List<TransactionExclusion>
}
