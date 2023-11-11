package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.users.ExternalUser
import nl.jvandis.teambalance.api.users.expose

data class AttendeesResponse(val attendees: List<AttendeeResponse>)

data class AttendeeResponse(
    val id: Long,
    val eventId: Long,
    val state: Availability,
    val user: ExternalUser,
)

fun Attendee.expose() =
    AttendeeResponse(
        id = id,
        eventId = eventId,
        state = availability,
        user = user.expose(),
    )
