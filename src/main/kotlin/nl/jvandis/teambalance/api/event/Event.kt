package nl.jvandis.teambalance.api.event

import org.springframework.data.jpa.repository.Temporal
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.TemporalType

@Entity
@Inheritance(
    strategy = InheritanceType.JOINED
)
abstract class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false, unique = true)
    @Temporal(TemporalType.TIMESTAMP)
    open val startTime: LocalDateTime,

    @Column(nullable = false)
    open val location: String,

    @Column(nullable = true)
    open val comment: String?
) {
    constructor() : this(startTime = LocalDateTime.MIN, location = "", comment = null)
}
