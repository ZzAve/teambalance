package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import org.slf4j.LoggerFactory
import toCalendar
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("USER_ID", "EVENT_ID"))))
data class Attendee(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @ManyToOne @JoinColumn(name = "USER_ID") val user: User,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val availability: Availability,
    @ManyToOne @JoinColumn(name = "EVENT_ID") val event: Event
) {
    companion object {
        private val log = LoggerFactory.getLogger(Attendee::class.java)
        private val dummyUser = User("dummy", Role.COACH)
        private val dummyEvent = Training(LocalDateTime.MIN.toCalendar(), "", "")
    }

    /**
     * no-args constructor
     */
    protected constructor() : this(dummyUser, dummyEvent)

    constructor(user: User, event: Event) : this(
        user = user,
        event = event,
        availability = Availability.NOT_RESPONDED
    )
    constructor(user: User, event: Event, availability: Availability) : this(
        id = 0,
        user = user,
        availability = availability,
        event = event
    )

    fun externalize() = AttendeeResponse(
        id = id,
        eventId = event.id,
        state = availability,
        user = user
    )
}

enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED
}
