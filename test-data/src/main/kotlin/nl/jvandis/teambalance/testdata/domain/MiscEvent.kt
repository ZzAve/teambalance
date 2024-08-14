package nl.jvandis.teambalance.testdata.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateMiscEvent(
    val startTime: LocalDateTime,
    val title: String?,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = emptyList(),
)

@Serializable
data class MiscEvent(
    val id: String,
    val startTime: LocalDateTime,
    val title: String,
    val location: String,
    val comment: String?,
    val attendees: List<Attendee>,
)
