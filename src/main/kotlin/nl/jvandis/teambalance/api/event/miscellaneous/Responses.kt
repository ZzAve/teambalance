package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.attendees.AttendeeResponse
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
    val userIds: List<Long>? = null
) {
    fun internalize(): MiscellaneousEvent = MiscellaneousEvent(
        startTime = startTime,
        comment = comment,
        location = location,
        title = title
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
