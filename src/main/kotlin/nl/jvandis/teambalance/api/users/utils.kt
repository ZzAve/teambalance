package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event

fun User.toAttendee(event: Event) = Attendee(
    user = this,
    event = event
)
