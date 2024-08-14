package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.users.ExternalUser
import nl.jvandis.teambalance.api.users.expose

data class AttendeesResponse(val attendees: List<AttendeeResponse>)

data class AttendeeResponse(
    val id: String,
    val eventId: String,
    val state: Availability,
    val user: ExternalUser,
)

fun Attendee.expose() =
    AttendeeResponse(
        id = teamBalanceId.value,
        eventId = eventId.value,
        state = availability,
        user = user.expose(),
    )
