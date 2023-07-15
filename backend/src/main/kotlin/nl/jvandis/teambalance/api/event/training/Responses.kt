package nl.jvandis.teambalance.api.event.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.CreateRecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesResponse
import nl.jvandis.teambalance.api.event.expose
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import nl.jvandis.teambalance.api.users.User
import java.time.LocalDateTime

data class UserAddRequest(
    val userId: Long
)

data class UpdateTrainerRequest(
    val userId: Long?
)

data class UpdateTrainingRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val comment: String?,
    val recurringEventProperties: RecurringEventPropertiesRequest?
)

data class PotentialTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = null,
    val recurringEventProperties: CreateRecurringEventPropertiesRequest? = null
) {
    fun internalize(): List<Training> = recurringEventProperties?.let {
        val recurringEventProperties = it.internalize()
        it
            .getRecurringEventDates(startTime)
            .map { e ->
                Training(
                    startTime = e,
                    comment = comment,
                    location = location,
                    recurringEventProperties = recurringEventProperties
                )
            }
    } ?: listOf(
        Training(
            startTime = startTime,
            comment = comment,
            location = location,
            recurringEventProperties = null
        )
    )
}

data class TrainingResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>,
    val trainer: User?,
    val recurringEventProperties: RecurringEventPropertiesResponse?
)

fun Training.expose() = expose(attendees ?: emptyList())
fun Training.expose(includeInactiveUsers: Boolean) = expose(
    attendees
        ?.filter { a -> includeInactiveUsers || a.user.isActive }
        ?: emptyList()
)

fun List<Training>.expose(attendees: List<Attendee>) = map { it.expose(attendees) }
fun Training.expose(attendees: List<Attendee>) = TrainingResponse(
    id = id,
    comment = comment,
    location = location,
    startTime = startTime,
    attendees = attendees.map(Attendee::expose),
    trainer = trainer,
    recurringEventProperties = recurringEventProperties?.expose()
)
