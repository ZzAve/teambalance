package nl.jvandis.teambalance.api.match

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import nl.jvandis.teambalance.api.event.Event
import java.time.LocalDateTime

interface TeamEventsRepository<T : Event> {
    fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable
    ): Page<T>
}
