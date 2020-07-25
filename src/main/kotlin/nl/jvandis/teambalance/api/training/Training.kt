package nl.jvandis.teambalance.api.training

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType

@Entity
@Inheritance(
    strategy = InheritanceType.JOINED
)
abstract class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open val startTime: Instant,

    @Column(nullable = false)
    open val location: String,

    @Column(nullable = true)
    open val comment: String?
) {
    constructor() : this(startTime = Instant.MIN, location = "", comment = null)
}

@Entity
data class Training(
    override val id: Long,
    override val startTime: Instant,
    override val location: String,
    override val comment: String? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: Instant, location: String, comment: String) :
        this(id = 0, startTime = startTime, location = location, comment = comment)
}

@Entity
data class Match(
    override val id: Long,
    override val startTime: Instant,
    override val location: String,
    override val comment: String? = null,
    @Column(nullable = false) val opponent: String,
    @Column(nullable = false) @Enumerated(EnumType.STRING) val homeAway: Place
) : Event(id, startTime, location, comment) {
    constructor(startTime: Instant, location: String, comment: String) :
        this(0, startTime, location, comment, "opponent", Place.HOME)
}

enum class Place {
    HOME,
    AWAY
}
