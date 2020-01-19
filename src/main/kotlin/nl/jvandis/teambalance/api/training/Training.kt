package nl.jvandis.teambalance.api.training

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

sealed class Event {
    abstract val id: Long
    abstract val startTime: Instant
    abstract val location: String
    //    abstract val attendees: List<Attendee>
    abstract val comment: String
}

@Entity
data class Training(
        @Id @GeneratedValue
        override val id: Long,

        @Column
        override val startTime: Instant,

        @Column
        override val location: String,

        @Column
        override val comment: String
) : Event() {
    constructor(startTime: Instant, location: String, comment: String) :
            this(0, startTime, location, comment)
}



