package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.Place
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import java.time.LocalDateTime
import java.util.UUID

data class UpdateMatchRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val opponent: String?,
    val homeAway: Place?,
    val comment: String?,
    val coach: String?
)

data class PotentialMatch(
    val startTime: LocalDateTime,
    val location: String,
    val opponent: String,
    val homeAway: Place,
    val comment: String?,
    val userIds: List<Long>? = null,
    val recurringEventProperties: RecurringEventPropertiesRequest? = null
) {
    fun internalize(): List<Match> = recurringEventProperties?.let {
        it
            .getRecurringEventDates(startTime)
            .map { e ->
                Match(
                    startTime = e,
                    location = location,
                    opponent = opponent,
                    homeAway = homeAway,
                    comment = comment,
                    recurringEventId = UUID.randomUUID()
                )
            }.let { listOf() }
    } ?: listOf(
        Match(
            startTime = startTime,
            location = location,
            opponent = opponent,
            homeAway = homeAway,
            comment = comment,
            recurringEventId = null
        )
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
    val homeAway: Place,
    val coach: String?
)

fun Match.expose() = expose(attendees ?: emptyList())
fun Match.expose(includeInactiveUsers: Boolean) = expose(
    attendees
        ?.filter { a -> includeInactiveUsers || a.user.isActive }
        ?: emptyList()
)

fun Match.expose(attendees: List<Attendee>) = MatchResponse(
    id = id,
    comment = comment,
    location = location,
    startTime = startTime,
    attendees = attendees.map(Attendee::expose),
    coach = coach,
    opponent = opponent,
    homeAway = homeAway
)
