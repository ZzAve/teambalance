package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.AttendeeResponse
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
    val attendees: List<AttendeeResponse>
)
