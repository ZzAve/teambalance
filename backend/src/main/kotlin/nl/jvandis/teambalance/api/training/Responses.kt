package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
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
    val comment: String?
)

data class PotentialTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = null
) {
    fun internalize(): Training = Training(
        startTime = startTime,
        comment = comment,
        location = location
    )
}

data class TrainingsResponse(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val trainings: List<TrainingResponse>
)

data class TrainingResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>,
    val trainer: User?
)
fun Training.expose() = expose(attendees ?: emptyList())
fun Training.expose(includeInactiveUsers: Boolean) = expose(
    attendees
        ?.filter { a -> includeInactiveUsers || a.user.isActive }
        ?: emptyList()
)
fun Training.expose(attendees: List<Attendee>) = TrainingResponse(
    id = id,
    comment = comment,
    location = location,
    startTime = startTime,
    attendees = attendees.map(Attendee::expose),
    trainer = trainer
)
