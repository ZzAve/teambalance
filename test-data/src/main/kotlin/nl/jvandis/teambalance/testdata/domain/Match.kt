package nl.jvandis.teambalance.testdata.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateMatch(
    val startTime: LocalDateTime,
    val location: String,
    val opponent: String,
    val homeAway: Place,
    val comment: String?,
    val userIds: List<Long>? = emptyList(),
)

@Serializable
enum class Place {
    HOME,
    AWAY,
}

@Serializable
data class Match(
    val id: Long,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<Attendee>,
    val opponent: String,
    val homeAway: Place,
    val coach: String?,
)
