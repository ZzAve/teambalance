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
        val teamBalanceId: String,
        val userId: Long,
        val availability: Availability,
        var eventId: String? = null,
        var user: User.Builder? = null,
    ) : TeamBalanceEntityBuilder<Attendee> {
        override fun build(): Attendee {
            val user = checkNotNull(user) { "User was not set" }
            val eventId = checkNotNull(eventId) { "EventId was not set" }
            return Attendee(
                id = id,
                teamBalanceId = TeamBalanceId(teamBalanceId),
                user = user.build(),
                availability = availability,
                eventId = TeamBalanceId(eventId),
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
