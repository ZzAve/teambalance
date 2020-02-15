package nl.jvandis.teambalance.api.training

import java.time.Instant
import javax.persistence.*


@Entity
@Inheritance(
        strategy = InheritanceType.JOINED
)
open class Event (
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        open val id: Long,

        @Column(nullable = false)
        open val startTime: Instant,

        @Column(nullable = false)
        open val location: String,

        @Column(nullable = true)
        open val comment: String?
) {
    constructor() : this(-1, Instant.MIN,"",null)
}

@Entity
data class Training(
        override val id: Long,
        override val startTime: Instant,
        override val location: String,
        override val comment: String? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: Instant, location: String, comment: String) :
            this(0, startTime, location, comment)
}


@Entity
data class Match(
        override val id: Long,
        override val startTime: Instant,
        override val location: String,
        override val comment: String? = null,
        @Column(nullable=false) val opponent: String,
        @Column(nullable=false) @Enumerated(EnumType.STRING)  val homeAway: Place
) : Event(id, startTime, location, comment) {
    constructor(startTime: Instant, location: String, comment: String) :
            this(0, startTime, location, comment, "opponent", Place.HOME)
}

enum class Place {
    HOME,
    AWAY

}



