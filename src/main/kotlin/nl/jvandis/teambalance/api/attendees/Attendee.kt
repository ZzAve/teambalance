package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.persistence.*

@Entity
@Table(uniqueConstraints=arrayOf(UniqueConstraint(columnNames=arrayOf("USER_ID", "TRAINING_ID"))))
data class Attendee(
        @Id @GeneratedValue val id: Long = -1,
        @ManyToOne @JoinColumn(name = "USER_ID") val user: User,
        @Enumerated(EnumType.STRING) @Column(nullable = false) val availability: Availability,
        @ManyToOne @JoinColumn(name="TRAINING_ID") val training: Training
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
            availability = Availability.NOT_RESPONDED,
            training = training)
}


enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED
}