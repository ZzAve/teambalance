package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder

data class Attendee(
    val id: Long = NO_ID,
    val teamBalanceId: TeamBalanceId,
    val user: User,
    val availability: Availability,
    val eventId: TeamBalanceId,
) {
    constructor(user: User, eventId: TeamBalanceId) : this(
        teamBalanceId = TeamBalanceId.random(),
        user = user,
        eventId = eventId,
        availability = Availability.NOT_RESPONDED,
    )

    data class Builder(
        val id: Long,
        val teamBalanceId: TeamBalanceId,
        val userId: TeamBalanceId,
        val availability: Availability,
        var eventId: TeamBalanceId? = null,
        var user: User? = null,
    ) : TeamBalanceEntityBuilder<Attendee> {
        override fun build(): Attendee {
            val user = checkNotNull(user) { "User was not set" }
            val eventId = checkNotNull(eventId) { "EventId was not set" }
            return Attendee(
                id = id,
                teamBalanceId = teamBalanceId,
                user = user,
                availability = availability,
                eventId = eventId,
            )
        }
    }
}

enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED,
}
