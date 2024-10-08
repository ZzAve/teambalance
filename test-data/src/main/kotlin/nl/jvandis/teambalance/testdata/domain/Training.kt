package nl.jvandis.teambalance.testdata.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = emptyList(),
)

@Serializable
data class Training(
    val id: String,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<String>? = null,
    val trainer: User? = null,
)
