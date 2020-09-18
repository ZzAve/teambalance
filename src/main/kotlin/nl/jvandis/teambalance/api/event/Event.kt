package nl.jvandis.teambalance.api.event

import toCalendar
import java.time.LocalDateTime
import java.util.Calendar
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Temporal
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
    open val startTime: Calendar,

    @Column(nullable = false)
    open val location: String,

    @Column(nullable = true)
    open val comment: String?
) {
    constructor() : this(startTime = LocalDateTime.MIN.toCalendar(), location = "", comment = null)
}
