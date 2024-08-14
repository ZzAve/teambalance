package nl.jvandis.teambalance.api.users

import nl.jvandis.teambalance.api.attendees.Availability
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.PotentialAttendee

fun User.toBeInsertedAttendee(event: Event) =
    PotentialAttendee(
        user = this,
        internalEventId = event.id,
        availability = Availability.NOT_RESPONDED,
    )
