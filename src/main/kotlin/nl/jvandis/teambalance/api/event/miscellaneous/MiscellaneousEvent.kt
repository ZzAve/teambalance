package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.event.Event
import java.time.LocalDateTime
import javax.persistence.Entity

@Entity
data class MiscellaneousEvent(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    val title: String? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String?, title: String?) :
        this(id = 0, startTime = startTime, location = location, comment = comment, title = title)

    fun createUpdatedEvent(updateEventRequest: UpdateMiscellaneousEventRequest) = copy(
        startTime = updateEventRequest.startTime ?: startTime,
        comment = updateEventRequest.comment ?: comment,
        location = updateEventRequest.location ?: location,
        title = updateEventRequest.title ?: title
    )

    fun externalizeWithAttendees(attendees: List<Attendee>): MiscellaneousEventResponse {
        val attendeesResponse = attendees.map { a -> a.externalize() }
        return externalize(attendeesResponse)
    }

    private fun externalize(attendeesResponse: List<AttendeeResponse>) = MiscellaneousEventResponse(
        id = id,
        comment = comment,
        title = title,
        location = location,
        startTime = startTime,
        attendees = attendeesResponse
    )
}
