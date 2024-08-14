package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.api.attendees.Availability
import nl.jvandis.teambalance.api.users.User

data class PotentialAttendee(val user: User, val availability: Availability, val internalEventId: Long)
