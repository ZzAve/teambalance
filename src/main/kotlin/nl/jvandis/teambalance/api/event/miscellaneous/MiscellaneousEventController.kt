package nl.jvandis.teambalance.api.event.miscellaneous

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidMiscellaneousEventException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toAttendee
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.math.min

@RestController
@Tag(name = "miscellaneous-events")
@RequestMapping(path = ["/api/miscellaneous-events"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MiscellaneousEventController(
    private val eventRepository: MiscellaneousEventRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getEvents(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(
            value = "since",
            defaultValue = "2022-08-01T00:00"
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: LocalDateTime,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        @RequestParam(value = "page", defaultValue = "1") page: Int,
    ): MiscellaneousEventsResponse {
        log.debug("GetAllMiscellaneousEvents")

        // In case of testing performance again :)
        // measureTiming(50) {
        //     getEventsAndAttendees(
        //         repository = eventRepository,
        //         attendeeRepository = attendeeRepository,
        //         page = page,
        //         limit = limit,
        //         since = since,
        //         includeAttendees = includeAttendees
        //     ).toResponse()
        // }

        return getEventsAndAttendees(
            eventsRepository = eventRepository,
            attendeeRepository = attendeeRepository,
            page = page,
            limit = limit,
            since = since,
            includeAttendees = includeAttendees
        ).toResponse(includeInactiveUsers)
    }

    private fun Pair<Page<MiscellaneousEvent>, Map<Long, List<Attendee>>>.toResponse(includeInactiveUsers: Boolean): MiscellaneousEventsResponse {
        val eventPage = first
        val attendees = second

        return MiscellaneousEventsResponse(
            totalPages = eventPage.totalPages,
            totalSize = eventPage.totalElements,
            page = eventPage.number + 1,
            size = min(eventPage.size, eventPage.content.size),
            events = eventPage.content.map { t ->
                val relevantAttendees = attendees[t.id]
                    ?.filter { a -> a.user.isActive || includeInactiveUsers }
                    ?: emptyList()
                t.externalizeWithAttendees(relevantAttendees)
            }
        )
    }

    @GetMapping("/{event-id}")
    fun getEvent(
        @PathVariable("event-id") eventId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
    ): MiscellaneousEventResponse {
        log.debug("Get miscellaneous event $eventId")

        val event = eventRepository.findByIdOrNull(eventId) ?: throw InvalidMiscellaneousEventException(eventId)
        val attendees =
            if (!includeAttendees) emptyList()
            else attendeeRepository
                .findAllByEventIdIn(listOf(eventId))
                .filter { a -> a.user.isActive || includeInactiveUsers }

        return event.externalizeWithAttendees(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createMiscellaneousEvent(
        @RequestBody potentialEvent: PotentialMiscellaneousEvent,
    ): MiscellaneousEventResponse {
        log.debug("postEvent $potentialEvent")

        val allUsers = userRepository.findAll()
        val event = potentialEvent.internalize()

        val requestedUsersToAdd = allUsers.filter {
            potentialEvent.userIds?.any { a -> a == it.id }
                ?: true
        }

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException("Not all requested userIds exists unfortunately ${potentialEvent.userIds}. Please verify your userIds")
        }

        val attendees = requestedUsersToAdd.map { it.toAttendee(event) }

        val savedEvent = eventRepository.save(event)
        val savedAttendees = attendeeRepository.saveAll(attendees).toList()

        return savedEvent.externalizeWithAttendees(savedAttendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{event-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "event-id") eventId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,

    ): List<Attendee> {
        log.debug("Adding: $user (or all: $addAll) to event $eventId")

        val event = eventRepository.findByIdOrNull(eventId) ?: throw InvalidMiscellaneousEventException(eventId)
        val users = if (addAll) {
            userRepository.findAll()
        } else {
            userRepository
                .findByIdOrNull(user.userId)
                ?.let(::listOf)
                ?: throw InvalidUserException(user.userId)
        }

        return users.map { u ->
            attendeeRepository.save(u.toAttendee(event))
        }
    }

    @PutMapping("/{event-id}")
    fun updateEvent(
        @PathVariable(value = "event-id") eventId: Long,
        @RequestBody updateEventRequest: UpdateMiscellaneousEventRequest,

    ): MiscellaneousEventResponse {

        return eventRepository
            .findByIdOrNull(eventId)
            ?.let {
                val updatedEvent = it.createUpdatedEvent(updateEventRequest)
                eventRepository.save(updatedEvent)
            }
            ?.externalizeWithAttendees(emptyList())
            ?: throw InvalidMiscellaneousEventException(eventId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{event-id}")
    fun deleteEvent(
        @PathVariable(value = "event-id") eventId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,

    ) {
        log.debug("Deleting event: $eventId")

        if (deleteAttendees) {
            attendeeRepository.findAllByEventIdIn(listOf(eventId))
                .let { attendeeRepository.deleteAll(it) }
        }
        try {
            eventRepository.deleteById(eventId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Event $eventId could not be deleted. There are still attendees bound to this event")
        }
    }
}
