package nl.jvandis.teambalance.api.event.match

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidMatchException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.DeletedEventsResponse
import nl.jvandis.teambalance.api.event.EventsResponse
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.event.training.UserAddRequest
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toNewAttendee
import nl.jvandis.teambalance.filters.START_OF_SEASON_RAW
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
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
@Tag(name = "matches")
@RequestMapping(path = ["/api/matches"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MatchController(
    private val eventRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
    private val matchService: MatchService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getMatches(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(defaultValue = START_OF_SEASON_RAW)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        since: LocalDateTime,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "1") page: Int,
    ): EventsResponse<MatchResponse> {
        log.debug("GetAllMatches")

        // In case of testing performance again :)
        // measureTiming(50) { getEventsAndAttendees(matchRepository, attendeeRepository, page, limit, since, includeAttendees).toResponse()}

        return getEventsAndAttendees(
            eventsRepository = eventRepository,
            page = page,
            limit = limit,
            since = since,
        ).toResponse(includeInactiveUsers)
    }

    private fun Page<Match>.toResponse(includeInactiveUsers: Boolean) =
        EventsResponse(
            totalPages = totalPages,
            totalSize = totalElements,
            page = number + 1,
            size = min(size, content.size),
            events = content.map { it.expose(includeInactiveUsers) },
        )

    @GetMapping("/{match-id}")
    fun getMatch(
        @PathVariable("match-id") matchId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
    ): MatchResponse {
        log.debug("Get match $matchId")
        val match = eventRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        val attendees =
            if (!includeAttendees) {
                emptyList()
            } else {
                attendeeRepository
                    .findAllByEventIdIn(listOf(matchId))
                    .filter { a -> a.user.isActive || includeInactiveUsers }
            }

        return match.expose(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createMatch(
        @RequestBody @Valid
        potentialEvent: PotentialMatch,
    ): EventsResponse<MatchResponse> {
        log.debug("postMatch {}", potentialEvent)
        val allUsers = userRepository.findAll()
        val events = potentialEvent.internalize()
        log.info("Requested to create match events: $events")

        val requestedUsersToAdd =
            allUsers.filter { user ->
                potentialEvent.userIds?.any { it == user.id }
                    ?: true
            }
        log.info("Users to add to match events: $requestedUsersToAdd")

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

        val savedAttendeesByEvent =
            savedEvents
                .map { event -> requestedUsersToAdd.map { userToAdd -> userToAdd.toNewAttendee(event) } }
                .let { attendees ->
                    attendeeRepository
                        .insertMany(attendees.flatten())
                        .groupBy { it.eventId }
                }

        return savedEvents.map { it.expose(savedAttendeesByEvent[it.id] ?: listOf()) }
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also {
                log.info(
                    "Created ${it.totalSize} match events with recurringEventId: ${events.firstOrNull()?.recurringEventProperties}. " +
                        "First event date: ${it.events.firstOrNull()?.startTime}, last event date: ${it.events.lastOrNull()?.startTime} ",
                )

                it.events.forEach { e ->
                    log.info(
                        "Created event as part of '${events.firstOrNull()?.recurringEventProperties}': $e",
                    )
                }
            }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{match-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,
    ): List<AttendeeResponse> {
        log.debug("Adding: {} (or all: {}) to match {}", user, addAll, matchId)
        val match = eventRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        val users =
            if (addAll) {
                userRepository.findAll()
            } else {
                userRepository
                    .findByIdOrNull(user.userId)
                    ?.let(::listOf)
                    ?: throw InvalidUserException(user.userId)
            }

        return users.map { u ->
            attendeeRepository.insert(u.toNewAttendee(match)).expose()
        }
    }

    @PutMapping("/{match-id}")
    fun updateMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
        @RequestBody updateMatchRequest: UpdateMatchRequest,
    ): EventsResponse<MatchResponse> {
        log.info("Updating match $matchId with $updateMatchRequest")
        return matchService.updateMatch(matchId, affectedRecurringEvents, updateMatchRequest)
            .expose(attendees = emptyList())
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also { log.info("Updated match $matchId") }
    }

    @PutMapping("/{match-id}/coach")
    fun updateCoach(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestBody updateCoachRequest: UpdateCoachRequest,
    ): MatchResponse {
        return eventRepository
            .findByIdOrNull(matchId)
            ?.let {
                val potentialCoach = updateCoachRequest.coach
                eventRepository.updateCoach(it, potentialCoach)
                it.copy(
                    coach = potentialCoach,
                )
            }
            ?.expose(emptyList())
            ?.also { log.info("Updated trainer of training $matchId. New coach ${it.coach}") }
            ?: throw InvalidTrainingException(matchId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{match-id}")
    fun deleteMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
    ): DeletedEventsResponse {
        log.info("Deleting match: $matchId")
        if (deleteAttendees) {
            attendeeRepository.findAllAttendeesBelongingToEvent(matchId, affectedRecurringEvents)
                .let { attendeeRepository.deleteAll(it) }
        }

        try {
            return eventRepository.deleteById(matchId, affectedRecurringEvents)
                .let(::DeletedEventsResponse)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Match $matchId could not be deleted. There are still attendees bound to this match")
        }
    }
}
