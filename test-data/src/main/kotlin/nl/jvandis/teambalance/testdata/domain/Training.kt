package nl.jvandis.teambalance.testdata.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateRecurringEventProperties(
    val intervalAmount: Int,
    val intervalTimeUnit: String,
    val amountLimit: Int? = null,
    val dateLimit: String? = null,
    val selectedDays: List<String>,
)

@Serializable
data class RecurringEventPropertiesResponse(
    val id: String,
    val intervalAmount: Int,
    val intervalTimeUnit: String,
    val amountLimit: Int? = null,
    val dateLimit: String? = null,
    val selectedDays: List<String>,
)

@Serializable
data class CreateTraining(
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<Long>? = emptyList(),
    val recurringEventProperties: CreateRecurringEventProperties? = null,
)

@Serializable
data class UpdateRecurringEventProperties(
    val id: String,
    val intervalAmount: Int,
    val intervalTimeUnit: String,
    val amountLimit: Int? = null,
    val dateLimit: String? = null,
    val selectedDays: List<String>,
)

@Serializable
data class UpdateTraining(
    val startTime: LocalDateTime? = null,
    val location: String? = null,
    val comment: String? = null,
    val recurringEventProperties: UpdateRecurringEventProperties? = null,
)

@Serializable
data class Training(
    val id: String,
    val startTime: LocalDateTime,
    val location: String,
    val comment: String?,
    val userIds: List<String>? = null,
    val trainer: User? = null,
    val recurringEventProperties: RecurringEventPropertiesResponse? = null,
)
