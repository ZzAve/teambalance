package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.match.TeamEventsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime

fun <T : Event> getEventsAndAttendees(
    eventsRepository: TeamEventsRepository<T>,
    attendeeRepository: AttendeeRepository,
    page: Int,
    limit: Int,
    since: LocalDateTime,
    includeAttendees: Boolean
): Pair<Page<T>, Map<Long, List<Attendee>>> {
    val pageRequest = PageRequest.of(page - 1, limit, Sort.by("startTime").ascending())
    val events = eventsRepository.findAllWithStartTimeAfter(since, pageRequest)

    val attendees = if (includeAttendees) {
        val matchIds = events.content.map { it.id }
        attendeeRepository.findAllByEventIdIn(matchIds)
            .groupBy { it.event.id }
    } else {
        emptyMap()
    }

    return Pair(events, attendees)
}
