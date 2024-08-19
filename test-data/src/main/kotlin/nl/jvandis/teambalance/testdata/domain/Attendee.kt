package nl.jvandis.teambalance.testdata.domain

import kotlinx.serialization.Serializable

@Serializable
data class CreateAttendee(
    val eventId: String,
    val userId: String,
    val availability: Availability? = null,
)

@Serializable
data class Attendee(
    val id: String,
    val eventId: String,
    val state: Availability,
    val user: User,
)

@Serializable
enum class Availability {
    PRESENT,
    ABSENT,
    UNCERTAIN,
    NOT_RESPONDED,
}
