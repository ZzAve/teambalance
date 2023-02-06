package nl.jvandis.teambalance.api.match

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidMatchException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.training.UserAddRequest
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toNewAttendee
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
import javax.validation.Valid
import kotlin.math.min

@RestController
@Tag(name = "matches")
@RequestMapping(path = ["/api/matches"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MatchController(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getMatches(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(value = "since", defaultValue = "2022-08-01T00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        since: LocalDateTime,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        @RequestParam(value = "page", defaultValue = "1") page: Int
    ): MatchesResponse {
        log.debug("GetAllMatches")

        // In case of testing performance again :)
        // measureTiming(50) { getEventsAndAttendees(matchRepository, attendeeRepository, page, limit, since, includeAttendees).toResponse()}

        return getEventsAndAttendees(
            eventsRepository = matchRepository,
            page = page,
            limit = limit,
            since = since,
            includeAttendees = includeAttendees
        ).toResponse(includeInactiveUsers)
    }

    private fun Page<Match>.toResponse(includeInactiveUsers: Boolean) = MatchesResponse(
        totalPages = totalPages,
        totalSize = totalElements,
        page = number + 1,
        size = min(size, content.size),
        matches = content.map { it.expose(includeInactiveUsers) }
    )

    @GetMapping("/{match-id}")
    fun getMatch(
        @PathVariable("match-id") matchId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean

    ): MatchResponse {
        log.debug("Get match $matchId")

        val match = matchRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
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
        potentialEvent: PotentialMatch
    ): MatchResponse {
        log.debug("postMatch $potentialEvent")

        val allUsers = userRepository.findAll()
        val matchToSave = potentialEvent.internalize()

        val requestedUsersToAdd = allUsers.filter { user ->
            potentialEvent.userIds?.any { it == user.id }
                ?: true
        }

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException("Not all requested userIds exists unfortunately ${potentialEvent.userIds}. Please verify your userIds")
        }

        val savedMatch = matchRepository.insert(matchToSave)

        val attendees = requestedUsersToAdd.map { it.toNewAttendee(savedMatch) }
        val savedAttendees = attendeeRepository.insertMany(attendees)

        return savedMatch.expose(savedAttendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{match-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest

    ): List<AttendeeResponse> {
        log.debug("Adding: $user (or all: $addAll) to match $matchId")

        val match = matchRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        val users = if (addAll) {
            userRepository.findAll()
        } else {
            userRepository
                .findByIdOrNull(user.userId)
                ?.let(::listOf)
                ?: throw InvalidUserException(user.userId)
        }

        return users.map {
            attendeeRepository.insert(it.toNewAttendee(match)).expose()
        }
    }

    @PutMapping("/{match-id}")
    fun updateMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestBody updateMatchRequest: UpdateMatchRequest

    ): MatchResponse {
        return matchRepository
            .findByIdOrNull(matchId)
            ?.let {
                val updatedMatch = it.createUpdatedMatch(updateMatchRequest)
                matchRepository.update(updatedMatch)
            }
            ?.expose(emptyList())
            ?: throw InvalidMatchException(matchId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{match-id}")
    fun deleteMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean

    ) {
        log.debug("Deleting match: $matchId")
        if (deleteAttendees) {
            attendeeRepository.findAllByEventIdIn(listOf(matchId))
                .let { attendeeRepository.deleteAll(it) }
        }
        try {
            matchRepository.deleteById(matchId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Match $matchId could not be deleted. There are still attendees bound to this match")
        }
    }
}
