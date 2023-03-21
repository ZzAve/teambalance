package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.NO_ID
import java.time.LocalDateTime
import java.util.UUID

abstract class Event(
    open val id: Long = NO_ID,
    open val startTime: LocalDateTime,
    open val location: String,
    open val comment: String?,
    open val recurringEventId: UUID? // TODO: persist the recurringEventId when introducing editing / deleting multiple entries of a recurringEvent
) {

    data class Builder(
        val id: Long,
        val startTime: LocalDateTime,
        val location: String,
        val comment: String?,
        val recurringEventId: UUID?
    )
}
