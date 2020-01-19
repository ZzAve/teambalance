package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.persistence.*

@Entity
data class Attendee(
        @Id @GeneratedValue val id: Long = -1,
        @ManyToOne val user: User,
        @Enumerated(EnumType.STRING) @Column(nullable = false) val state: Availability,
        @ManyToOne val training: Training
) {
    companion object {
        private val log = LoggerFactory.getLogger(javaClass)
        private val dummyUser = User("dummy", Role.COACH)
        private val dummyTraining = Training(Instant.EPOCH, "", "");
    }

    /**
     * no-args constructor
     */
    protected constructor() : this(dummyUser, dummyTraining) //I do not like this
    {
        log.warn("I got called. unfortunately :${toString()}")
    }

    constructor(user: User, training: Training) : this(
            user = user,
            state = Availability.NOT_RESPONDED,
            training = training)
}


enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED
}