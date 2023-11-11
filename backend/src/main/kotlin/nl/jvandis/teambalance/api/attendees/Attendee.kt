package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder
import org.slf4j.LoggerFactory

data class Attendee(
    val id: Long = NO_ID,
    val user: User,
    val availability: Availability,
    val eventId: Long,
) {
    companion object {
        private val log = LoggerFactory.getLogger(Attendee::class.java)
    }

    constructor(user: User, eventId: Long) : this(
        user = user,
        eventId = eventId,
        availability = Availability.NOT_RESPONDED,
    )

    data class Builder(
        val id: Long,
        val userId: Long,
        val eventId: Long,
        val availability: Availability,
        var user: User? = null,
    ) : TeamBalanceEntityBuilder<Attendee> {
        override fun build() =
            Attendee(
                id = id,
                user = checkNotNull(user) { "User has not been set." },
                availability = availability,
                eventId = eventId,
            )
    }
}

enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED,
}
