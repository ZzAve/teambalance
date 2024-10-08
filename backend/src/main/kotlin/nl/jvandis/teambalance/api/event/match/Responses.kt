package nl.jvandis.teambalance.api.event.match

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.CreateRecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesResponse
import nl.jvandis.teambalance.api.event.expose
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import java.time.LocalDateTime

data class UpdateMatchRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val opponent: String?,
    val homeAway: Place?,
    val comment: String?,
    val recurringEventProperties: RecurringEventPropertiesRequest?,
)

data class UpdateAdditionalInfoRequest(
    val additionalInfo: String,
)

data class PotentialMatch(
    val startTime: LocalDateTime,
    val location: String,
    val opponent: String,
    val homeAway: Place,
    val comment: String?,
    val userIds: List<String>? = null,
    val recurringEventProperties: CreateRecurringEventPropertiesRequest? = null,
) {
    fun internalize(): List<Match> =
        recurringEventProperties?.let {
            val recurringEventProperties = it.internalize()
            it
                .getRecurringEventDates(startTime)
                .map { e ->
                    Match(
                        startTime = e,
                        location = location,
                        opponent = opponent,
                        homeAway = homeAway,
                        comment = comment,
                        recurringEventProperties = recurringEventProperties,
                    )
                }
        } ?: listOf(
            Match(
                startTime = startTime,
                location = location,
                opponent = opponent,
                homeAway = homeAway,
                comment = comment,
                recurringEventProperties = null,
            ),
        )
}

data class MatchResponse(
    val id: String,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>,
    val opponent: String,
    val homeAway: Place,
    val additionalInfo: String?,
    val recurringEventProperties: RecurringEventPropertiesResponse?,
)

fun Match.expose() = expose(attendees ?: emptyList())

fun Match.expose(includeInactiveUsers: Boolean) =
    expose(
        attendees
            ?.filter { a -> includeInactiveUsers || a.user.isActive }
            ?: emptyList(),
    )

fun List<Match>.expose(attendees: List<Attendee>) = map { it.expose(attendees) }

fun Match.expose(attendees: List<Attendee>) =
    MatchResponse(
        id = teamBalanceId.value,
        comment = comment,
        location = location,
        startTime = startTime,
        attendees = attendees.map(Attendee::expose),
        additionalInfo = additionalInfo,
        opponent = opponent,
        homeAway = homeAway,
        recurringEventProperties = recurringEventProperties?.expose(),
    )
