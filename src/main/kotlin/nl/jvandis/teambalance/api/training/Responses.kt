package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.users.User
import java.time.Instant

data class UserAddRequest(
    val userId: Long
)

data class UpdateTrainingRequest(
    val startTime: Long?,
    val location: String?,
    val comment: String?
)

data class PotentialTraining(
    val startTime: Long,
    val location: String,
    val comment: String,
    val attendees: List<Long>
) {
    fun internalize(users: Iterable<User>): Training {
        val training = Training(
            startTime = Instant.ofEpochMilli(startTime),
            comment = comment,
            location = location
        )

        users.map { it.toAttendee(training) }
        return training
    }
}

fun User.toAttendee(event: Event) = Attendee(
    user = this,
    event = event
)

fun List<Attendee>.toTrainingResponse(trainingId: Long) = map {
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
    val startTime: Instant,
    val location: String,
    val comment: String?,
    val attendees: List<AttendeeResponse>?
)
