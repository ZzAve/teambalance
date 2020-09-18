import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

// package nl.jvandis.teambalance.api.event
//
// import io.micronaut.data.model.Page
// import io.micronaut.data.model.Sort
// import nl.jvandis.teambalance.api.attendees.Attendee
// import nl.jvandis.teambalance.api.attendees.AttendeeRepository
// import nl.jvandis.teambalance.api.match.TeamEventsRepository
// import java.time.LocalDateTime
//
// fun <T : Event> getEventsAndAttendees(
//     eventsRepository: TeamEventsRepository<T>,
//     attendeeRepository: AttendeeRepository,
//     page: Int,
//     limit: Int,
//     since: LocalDateTime,
//     includeAttendees: Boolean
// ): Pair<Page<T>, Map<Long, List<Attendee>>> {
//     // val pageRequest = PageRequest.of(page - 1, limit, Sort.of(Sort.Order("startTime")).ascending())
//     val events = eventsRepository.findAllWithStartTimeAfter(since, pageRequest)
//
//     val attendees = if (includeAttendees) {
//         val matchIds = events.content.map { it.id }
//         attendeeRepository.findAllByEventIdIn(matchIds)
//             .groupBy { it.event.id }
//     } else emptyMap()
//
//     return Pair(events, attendees)
// }


fun Calendar.toLocalDateTime(): LocalDateTime {
    val tz = timeZone
    val zid = if(tz == null)  ZoneId.systemDefault() else tz.toZoneId()
    return LocalDateTime.ofInstant(this.toInstant(), zid)
}

fun LocalDateTime.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(year, monthValue - 1, dayOfMonth, hour, minute, second)
    return calendar
}
