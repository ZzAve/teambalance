package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.NO_ID
import java.time.LocalDate
import java.time.LocalDateTime

abstract class Event(
    open val id: Long = NO_ID,
    open val startTime: LocalDateTime,
    open val location: String,
    open val comment: String?,
    open val recurringEventProperties: RecurringEventProperties?
) {

    data class Builder(
        val id: Long,
        val startTime: LocalDateTime,
        val location: String,
        val comment: String?,
        val recurringEventId: Long?,
        var recurringEventProperties: RecurringEventProperties?
    ) {
        fun validate() {
            recurringEventId?.let {
                check(recurringEventProperties != null) {
                    "recurringEventProperties was not set even though `recurringEventId` was  "
                }
                check(it == recurringEventProperties?.id) {
                    "recurringEventProperties.id ${recurringEventProperties?.id} was expected to be the same as reccuringEventId $recurringEventId"
                }
            }
        }
    }
}

data class RecurringEventProperties(
    val id: Long = NO_ID,
    val teamBalanceId: RecurringEventPropertiesId,
    val intervalAmount: Int,
    val intervalTimeUnit: TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<Int>
) {

    enum class TimeUnit {
        WEEK,
        MONTH
    }
}

enum class AffectedRecurringEvents {
    ALL,
    CURRENT_AND_FUTURE,
    CURRENT
}
