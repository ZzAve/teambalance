package nl.jvandis.teambalance.api.bank

import org.springframework.data.jpa.repository.Temporal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.TemporalType

data class TransactionExclusions(
    val transactionExclusions: List<TransactionExclusion>
)

@Entity
@Table
data class TransactionExclusion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    val date: LocalDate?,

    @Column(nullable = true) val transactionId: Int?,
    @Column(nullable = true) val counterParty: String?,
    @Column(nullable = true) val description: String?

) {

    constructor(date: LocalDate? = null, transactionId: Int? = null, counterParty: String? = null, description: String? = null) : this(
        0,
        date,
        transactionId,
        counterParty,
        description

    )
}
