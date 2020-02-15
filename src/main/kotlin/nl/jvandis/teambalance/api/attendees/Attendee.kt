package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.training.Event
import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.persistence.*

@Entity
@Table(uniqueConstraints=arrayOf(UniqueConstraint(columnNames=arrayOf("USER_ID", "EVENT_ID"))))
data class Attendee(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = -1,
        @ManyToOne @JoinColumn(name = "USER_ID") val user: User,
        @Enumerated(EnumType.STRING) @Column(nullable = false) val availability: Availability,
        @ManyToOne @JoinColumn(name="EVENT_ID") val event: Event
) {
    companion object {
        private val log = LoggerFactory.getLogger(Attendee::class.java)
        private val dummyUser = User("dummy", Role.COACH)
        private val dummyEvent = Training(Instant.EPOCH, "", "");
    }

    /**
     * no-args constructor
     */
    protected constructor() : this(dummyUser, dummyEvent)

    constructor(user: User, event: Event) : this(
            user = user,
            availability = Availability.NOT_RESPONDED,
            event = event
    )
}


enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED
}