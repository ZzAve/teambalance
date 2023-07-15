package nl.jvandis.teambalance.api.event

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Period

private val log = LoggerFactory.getLogger("nl.jvandis.teambalance.api.event.EventUtils")

fun <T : Event> getEventsAndAttendees(
    eventsRepository: TeamEventsRepository<T>,
    page: Int,
    limit: Int,
    since: LocalDateTime
): Page<T> {
    val pageRequest = PageRequest.of(page - 1, limit, Sort.by("startTime").ascending())

    return eventsRepository.findAllWithStartTimeAfter(since, pageRequest)
}

fun CreateRecurringEventPropertiesRequest.getRecurringEventDates(
    startTime: LocalDateTime
): List<LocalDateTime> {
    val interval = when (intervalTimeUnit) {
        RecurringEventProperties.TimeUnit.WEEK -> Period.ofWeeks(intervalAmount)
        RecurringEventProperties.TimeUnit.MONTH -> Period.ofMonths(intervalAmount)
    }

    val internalSelectedDays = selectedDays
    require(internalSelectedDays.contains(startTime.dayOfWeek)) {
        "Start time is not part of the selected days. " +
            "Please make sure the start time is at one of the selected days."
    }

    val eventDates: List<LocalDateTime>
    if (amountLimit != null) {
        eventDates = (1 until amountLimit).runningFold(startTime) { acc, _ ->
            nextEventDate(acc, interval, internalSelectedDays)
        }
    } else if (dateLimit != null) {
        require(dateLimit >= startTime.toLocalDate()) {
            "The dateLimit property should be later than the startTime for an event to be recurring."
        }

        val eventsToDateLimit = mutableListOf<LocalDateTime>()
        var nextEvent = startTime
        do {
            eventsToDateLimit.add(nextEvent)
            nextEvent = nextEventDate(nextEvent, interval, internalSelectedDays)
        } while (nextEvent.toLocalDate() <= dateLimit)

        eventDates = eventsToDateLimit.toList()
    } else {
        error("One of amountLimit or dateLimit should have been set")
    }
    log.debug("Derived event dates from $startTime and $this: $eventDates")
    return eventDates
}

fun nextEventDate(lastEventDate: LocalDateTime, interval: Period, daysOfWeek: List<DayOfWeek>): LocalDateTime {
    val nextWeekDayInRecurringEvent =
        daysOfWeek
            .filter { it.value > lastEventDate.dayOfWeek.value }
            .minByOrNull { it.value }

    return if (nextWeekDayInRecurringEvent == null) {
        // take first of the week and add interval
        lastEventDate
            .minusDays(0L + lastEventDate.dayOfWeek.value - daysOfWeek.minBy { it.value }.value)
            .plus(interval)
    } else {
        // add weekday diff to current date
        lastEventDate.plusDays(0L + nextWeekDayInRecurringEvent.value - lastEventDate.dayOfWeek.value)
    }
//    val daysTillNextEvent =
//        lastEventDate.dayOfWeek.value - nextWeekDayInRecurringEvent.value.let { if (it < 0) it + 7 else it }
//
//    return lastEventDate.plusDays(0L + daysTillNextEvent)
}

