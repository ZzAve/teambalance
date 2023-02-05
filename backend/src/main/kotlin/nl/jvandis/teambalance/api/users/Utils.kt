package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event

fun User.toNewAttendee(event: Event) = Attendee(
    user = this,
    eventId = event.id

)
