package nl.jvandis.teambalance.api.event.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.CreateRecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesResponse
import nl.jvandis.teambalance.api.event.expose
import nl.jvandis.teambalance.api.event.getRecurringEventDates
import nl.jvandis.teambalance.api.users.UserResponse
import nl.jvandis.teambalance.api.users.expose
import java.time.LocalDateTime

data class UpdateTrainerRequest(
    val userId: String?,
)

data class UpdateTrainingRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val comment: String?,
    val recurringEventProperties: RecurringEventPropertiesRequest?,
)

data class PotentialTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<String>? = null,
    val recurringEventProperties: CreateRecurringEventPropertiesRequest? = null,
) {
    fun internalize(): List<Training> =
        recurringEventProperties?.let {
            val recurringEventProperties = it.internalize()
            it
                .getRecurringEventDates(startTime)
                .map { e ->
                    Training(
                        startTime = e,
                        comment = comment,
                        location = location,
                        recurringEventProperties = recurringEventProperties,
                    )
                }
        } ?: listOf(
            Training(
                startTime = startTime,
                comment = comment,
                location = location,
                recurringEventProperties = null,
            ),
        )
}

data class TrainingResponse(
    val id: String,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>,
    val trainer: UserResponse?,
    val recurringEventProperties: RecurringEventPropertiesResponse?,
)

fun Training.expose(includeInactiveUsers: Boolean) =
    expose(
        attendees
            ?.filter { a -> includeInactiveUsers || a.user.isActive }
            ?: emptyList(),
    )

fun List<Training>.expose(attendees: List<Attendee>) = map { it.expose(attendees) }

fun Training.expose(attendees: List<Attendee>) =
    TrainingResponse(
        id = teamBalanceId.value,
        comment = comment,
        location = location,
        startTime = startTime,
        attendees = attendees.map(Attendee::expose),
        trainer = trainer?.expose(),
        recurringEventProperties = recurringEventProperties?.expose(),
    )
