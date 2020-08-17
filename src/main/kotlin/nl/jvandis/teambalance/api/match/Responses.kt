package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.event.Place
import java.time.LocalDateTime

data class UpdateMatchRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val comment: String?
)

data class PotentialMatch(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val homeAway: Place,
    val opponent: String,
    val attendees: List<Long>
) {
    fun internalize(): Match = Match(
        startTime = startTime,
        comment = comment,
        location = location,
        opponent = opponent,
        homeAway = homeAway
    )
}

data class MatchesResponse(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val matches: List<MatchResponse>
)

data class MatchResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>,
    val opponent: String,
    val homeAway: Place
)
