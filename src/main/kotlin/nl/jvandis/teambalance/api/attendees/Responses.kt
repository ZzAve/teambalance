package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.users.User

data class AttendeesResponse(val attendees: List<AttendeeResponse>)
data class AttendeeResponse(
        val id: Long,
        val eventId: Long,
        val state: Availability,
        val user: User
)

fun List<Attendee>.toResponse() = map {
    AttendeeResponse(
            id = it.id,
            eventId = it.event.id,
            state = it.availability,
            user = it.user
    )
}