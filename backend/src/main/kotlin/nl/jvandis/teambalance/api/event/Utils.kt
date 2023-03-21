package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest.TimeUnit
import nl.jvandis.teambalance.api.match.TeamEventsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.time.Period

fun <T : Event> getEventsAndAttendees(
    eventsRepository: TeamEventsRepository<T>,
    page: Int,
    limit: Int,
    since: LocalDateTime
): Page<T> {
    val pageRequest = PageRequest.of(page - 1, limit, Sort.by("startTime").ascending())

    return eventsRepository.findAllWithStartTimeAfter(since, pageRequest)
}

fun RecurringEventPropertiesRequest.getRecurringEventDates(
    startTime: LocalDateTime
): List<LocalDateTime> {
    val interval = when (intervalTimeUnit) {
        TimeUnit.WEEK -> Period.ofWeeks(intervalAmount)
        TimeUnit.MONTH -> Period.ofMonths(intervalAmount)
    }

    val eventDates: List<LocalDateTime>
    if (amountLimit != null) {
        eventDates = (0..amountLimit).runningFold(startTime) { acc, _ ->
            acc + interval
        }
    } else if (dateLimit != null) {
        val eventsToDateLimit = mutableListOf<LocalDateTime>()
        var latest = startTime
        do {
            eventsToDateLimit.add(latest)
            latest += interval
        } while (latest.toLocalDate() <= dateLimit)

        eventDates = eventsToDateLimit.toList()
    } else {
        error("One of amountLimit or dateLimit should have been set")
    }
    return eventDates
}
