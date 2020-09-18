package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.event.Event
import toCalendar
import toLocalDateTime
import java.time.LocalDateTime
import java.util.Calendar
import javax.persistence.Entity

@Entity
data class Training(
    override val id: Long,
    override val startTime: Calendar,
    override val location: String,
    override val comment: String? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: Calendar, location: String, comment: String?) :
        this(id = 0, startTime = startTime, location = location, comment = comment)

    constructor() : this(LocalDateTime.MIN.toCalendar(), "unknown",null)

    fun createUpdatedTraining(updateTrainingRequestBody: UpdateTrainingRequest) = copy(
        startTime = updateTrainingRequestBody.startTime?.toCalendar() ?: startTime,
        comment = updateTrainingRequestBody.comment ?: comment,
        location = updateTrainingRequestBody.location ?: location
    )

    fun externalizeWithAttendees(attendees: List<Attendee>): TrainingResponse {
        val attendeesResponse = attendees.map { a -> a.externalize() }
        return externalize(attendeesResponse)
    }

    private fun externalize(attendeesResponse: List<AttendeeResponse>) = TrainingResponse(
        id = id,
        comment = comment,
        location = location,
        startTime = startTime.toLocalDateTime(),
        attendees = attendeesResponse
    )
}
