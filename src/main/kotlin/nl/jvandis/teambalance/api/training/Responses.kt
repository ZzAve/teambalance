package nl.jvandis.teambalance.api.training

import com.fasterxml.jackson.annotation.JsonFormat
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.users.User
import java.time.LocalDateTime

data class UserAddRequest(
    val userId: Long
)

data class UpdateTrainingRequest(
    val startTime: LocalDateTime?,
    val location: String?,
    val comment: String?
)

data class PotentialTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String,
    val attendees: List<Long>
) {
    fun internalize(): Training = Training(
        startTime = startTime,
        comment = comment,
        location = location
    )
}

fun User.toAttendee(event: Event) = Attendee(
    user = this,
    event = event
)

fun Iterable<Attendee>.toTrainingResponse(trainingId: Long) = map {
    AttendeeResponse(
        id = it.id,
        eventId = trainingId,
        state = it.availability,
        user = it.user
    )
}

data class TrainingsResponse(val trainings: List<TrainingResponse>)
data class TrainingResponse(
    val id: Long,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>?
)
