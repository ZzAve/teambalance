package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.event.Event
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface TeamEventsRepository<T : Event> {
    fun findAllWithStartTimeAfter(
    since: LocalDateTime,
    pageable: Pageable
    ): Page<T>
}
