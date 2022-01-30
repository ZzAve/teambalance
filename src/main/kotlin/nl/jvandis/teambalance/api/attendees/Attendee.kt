package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.users.User
import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    name = "Attendee",
    indexes = [
        Index(name = "idx_attendee_event_id", columnList = "EVENT_ID")
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["USER_ID", "EVENT_ID"])
    ]
)
data class Attendee(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @ManyToOne @JoinColumn(name = "USER_ID") val user: User,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val availability: Availability,
    @ManyToOne @JoinColumn(name = "EVENT_ID") val event: Event
) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Attendee

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , availability = $availability )"
    }

}

enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED
}
