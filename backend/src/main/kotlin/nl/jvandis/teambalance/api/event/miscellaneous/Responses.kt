package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import java.time.LocalDateTime
import java.util.UUID

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
    val recurringEventProperties: RecurringEventPropertiesRequest? = null
) {
    fun internalize(): List<MiscellaneousEvent> =
        recurringEventProperties?.let {
            val recurringEventId = UUID.randomUUID()
            it
                .getRecurringEventDates(startTime)
                .map { e ->
                    MiscellaneousEvent(
                        startTime = e,
                        comment = comment,
                        location = location,
                        title = title,
                        recurringEventId = recurringEventId
                    )
                }
        } ?: listOf(
            MiscellaneousEvent(
                startTime = startTime,
                comment = comment,
                location = location,
                title = title,
                recurringEventId = null
            )
        )
}

data class MiscellaneousEventsResponse(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val events: List<MiscellaneousEventResponse>
)

data class MiscellaneousEventResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val title: String,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>
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
