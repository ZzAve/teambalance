package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.NO_ID
import java.time.LocalDateTime

abstract class Event(
    open val id: Long = NO_ID,
    open val startTime: LocalDateTime,
    open val location: String,
    open val comment: String?
) {

    data class Builder(
        val id: Long,
        val startTime: LocalDateTime,
        val location: String,
        val comment: String?
    )

}
