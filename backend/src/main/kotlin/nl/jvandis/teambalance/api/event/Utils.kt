package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.api.match.TeamEventsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

fun <T : Event> getEventsAndAttendees(
    eventsRepository: TeamEventsRepository<T>,
    page: Int,
    limit: Int,
    since: LocalDateTime
): Page<T> {
    val pageRequest = PageRequest.of(page - 1, limit, Sort.by("startTime").ascending())

    return eventsRepository.findAllWithStartTimeAfter(since, pageRequest)
}
