package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.CreateRecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import java.time.LocalDateTime

data class UserAddRequest(
    val userId: Long
)

data class UpdateMiscellaneousEventRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val comment: String?,
    val title: String?
)

data class PotentialMiscellaneousEvent(
    val startTime: LocalDateTime,
    val title: String?,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = null,
    val recurringEventProperties: CreateRecurringEventPropertiesRequest? = null
) {
    fun internalize(): List<MiscellaneousEvent> =
        recurringEventProperties?.let {
            it
                .getRecurringEventDates(startTime)
                .map { e ->
                    MiscellaneousEvent(
                        startTime = e,
                        comment = comment,
                        location = location,
                        title = title,
                        recurringEventProperties = it.internalize()
                    )
                }
        } ?: listOf(
            MiscellaneousEvent(
                startTime = startTime,
                comment = comment,
                location = location,
                title = title,
                recurringEventProperties = null
            )
        )
}

data class MiscellaneousEventResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val title: String,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>
//    val recurringEventProperties: RecurringEventPropertiesResponse?
)

// FIXME: attendees are part of event
fun MiscellaneousEvent.expose() = expose(attendees ?: emptyList())
fun MiscellaneousEvent.expose(includeInactiveUsers: Boolean) = expose(
    attendees
        ?.filter { a -> includeInactiveUsers || a.user.isActive }
        ?: emptyList()
)

fun MiscellaneousEvent.expose(attendees: List<Attendee>) = MiscellaneousEventResponse(
    id = id,
    comment = comment,
    title = title ?: "Overig event",
    location = location,
    startTime = startTime,
    attendees = attendees.map(Attendee::expose)

)
