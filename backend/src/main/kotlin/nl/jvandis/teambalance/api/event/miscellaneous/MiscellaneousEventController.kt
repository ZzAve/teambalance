package nl.jvandis.teambalance.api.event.miscellaneous

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidMiscellaneousEventException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.DeletedEventsResponse
import nl.jvandis.teambalance.api.event.EventsResponse
import nl.jvandis.teambalance.api.event.UserAddRequest
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toBeInsertedAttendee
import nl.jvandis.teambalance.filters.START_OF_SEASON_RAW
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
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
    private val miscellaneousEventService: MiscellaneousEventService,
    private val eventRepository: MiscellaneousEventRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getEvents(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(defaultValue = START_OF_SEASON_RAW)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        since: LocalDateTime,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "1") page: Int,
    ): EventsResponse<MiscellaneousEventResponse> {
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
            page = page,
            limit = limit,
            since = since,
        ).expose(includeInactiveUsers)
    }

    private fun Page<MiscellaneousEvent>.expose(includeInactiveUsers: Boolean) =
        EventsResponse(
            totalPages = totalPages,
            totalSize = totalElements,
            page = number + 1,
            size = min(size, content.size),
            events = content.map { it.expose(includeInactiveUsers) },
        )

    @GetMapping("/{event-id}")
    fun getEvent(
        @PathVariable("event-id") eventId: String,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
    ): MiscellaneousEventResponse {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.debug("Get miscellaneous event $eventTeamBalanceId")
        val event =
            eventRepository.findByIdOrNull(eventTeamBalanceId) ?: throw InvalidMiscellaneousEventException(
                eventTeamBalanceId,
            )
        val attendees =
            if (!includeAttendees) {
                emptyList()
            } else {
                attendeeRepository
                    .findAllByEventIdIn(listOf(eventTeamBalanceId))
                    .filter { a -> a.user.isActive || includeInactiveUsers }
            }

        return event.expose(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createMiscellaneousEvent(
        @RequestBody potentialEvent: PotentialMiscellaneousEvent,
    ): EventsResponse<MiscellaneousEventResponse> {
        log.debug("postEvent {}", potentialEvent)
        val allUsers = userRepository.findAll()
        val events = potentialEvent.internalize()
        log.info("Requested to create miscellaneous events: $events")

        val requestedUsersToAdd =
            allUsers.filter { user ->
                potentialEvent.userIds?.any { it == user.teamBalanceId.value }
                    ?: true
            }
        log.info("Users to add to miscellaneous events: $requestedUsersToAdd")

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException(
                "Not all requested userIds exists unfortunately ${potentialEvent.userIds}." +
                    " Please verify your userIds",
            )
        }

        val savedEvents =
            if (events.size > 1) {
                eventRepository.insertRecurringEvent(events)
            } else {
                listOf(eventRepository.insertSingleEvent(events.first()))
            }

        val savedAttendeesByEvent: Map<TeamBalanceId, List<Attendee>> =
            savedEvents
                .map { event -> requestedUsersToAdd.map { userToAdd -> userToAdd.toBeInsertedAttendee(event) } }
                .let { attendees ->
                    attendeeRepository
                        .insertMany(attendees.flatten())
                        .groupBy { it.eventId }
                }

        return savedEvents.map { it.expose(savedAttendeesByEvent[it.teamBalanceId] ?: listOf()) }
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also {
                log.info(
                    "Created ${it.totalSize} misc events with recurringEventId: ${it.events.firstOrNull()?.recurringEventProperties}. " +
                        "First event date: ${it.events.firstOrNull()?.startTime}, last event date: ${it.events.lastOrNull()?.startTime} ",
                )

                it.events.forEach { e ->
                    log.info(
                        "Created event as part of '${it.events.firstOrNull()?.recurringEventProperties}': $e",
                    )
                }
            }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{event-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "event-id") eventId: String,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,
    ): List<AttendeeResponse> {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.info("Adding: $user (or all: $addAll) to event $eventTeamBalanceId")
        val event =
            eventRepository.findByIdOrNull(eventTeamBalanceId) ?: throw InvalidMiscellaneousEventException(
                eventTeamBalanceId,
            )
        val users =
            if (addAll) {
                userRepository.findAll()
            } else {
                val userTeamBalanceId = TeamBalanceId(user.userId)
                userRepository
                    .findByIdOrNull(userTeamBalanceId)
                    ?.let(::listOf)
                    ?: throw InvalidUserException(userTeamBalanceId)
            }

        return users.map { u ->
            attendeeRepository.insert(event, u).expose()
        }
    }

    @PutMapping("/{event-id}")
    fun updateEvent(
        @PathVariable(value = "event-id") eventId: String,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
        @RequestBody updateEventRequest: UpdateMiscellaneousEventRequest,
    ): EventsResponse<MiscellaneousEventResponse> {
        val miscellaneousTeamBalanceId = TeamBalanceId(eventId)
        log.info("Updating miscellaneousEvent $miscellaneousTeamBalanceId with $updateEventRequest")

        return miscellaneousEventService.updateMiscellaneousEvent(miscellaneousTeamBalanceId, affectedRecurringEvents, updateEventRequest)
            .expose(attendees = emptyList())
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also { log.info("Updated miscellaneousEvent $miscellaneousTeamBalanceId") }
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{event-id}")
    @Transactional
    fun deleteEvent(
        @PathVariable(value = "event-id") eventId: String,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
    ): DeletedEventsResponse {
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.info("Deleting event: $eventTeamBalanceId")

        if (deleteAttendees) {
            attendeeRepository.findAllAttendeesBelongingToEvent(eventTeamBalanceId, affectedRecurringEvents)
                .let { attendeeRepository.deleteAll(it) }
        }

        try {
            return eventRepository.deleteById(eventTeamBalanceId, affectedRecurringEvents)
                .let(::DeletedEventsResponse)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException(
                "Event $eventTeamBalanceId could not be deleted. There are still attendees bound to this event",
            )
        }
    }
}
