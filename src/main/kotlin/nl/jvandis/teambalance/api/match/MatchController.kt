package nl.jvandis.teambalance.api.match

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidMatchException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.training.UserAddRequest
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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.validation.Valid
import kotlin.math.min

@RestController
@Api(tags = ["matches"])
@RequestMapping(path = ["/api/matches"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MatchController(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
    private val secretService: SecretService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getMatches(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(value = "since", defaultValue = "2020-09-01T00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: LocalDateTime,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        @RequestParam(value = "page", defaultValue = "1") page: Int,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): MatchesResponse {
        log.debug("GetAllMatches")
        secretService.ensureSecret(secret)

        // In case of testing performance again :)
        // measureTiming(50) { getEventsAndAttendees(matchRepository, attendeeRepository, page, limit, since, includeAttendees).toResponse()}

        return getEventsAndAttendees(
            eventsRepository = matchRepository,
            attendeeRepository = attendeeRepository,
            page = page,
            limit = limit,
            since = since,
            includeAttendees = includeAttendees
        ).toResponse()
    }

    private fun Pair<Page<Match>, Map<Long, List<Attendee>>>.toResponse(): MatchesResponse {
        val matchesPage = first
        val attendees = second
        return MatchesResponse(
            totalPages = matchesPage.totalPages,
            totalSize = matchesPage.totalElements,
            page = matchesPage.number + 1,
            size = min(matchesPage.size, matchesPage.content.size),
            matches = matchesPage.content.map { t ->
                val relevantAttendees = attendees[t.id] ?: emptyList()
                t.externaliseWithAttendees(relevantAttendees)
            }
        )
    }

    @GetMapping("/{match-id}")
    fun getMatch(
        @PathVariable("match-id") matchId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): MatchResponse {
        log.debug("Get match $matchId")
        secretService.ensureSecret(secret)

        val match = matchRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        val attendees =
            if (!includeAttendees) emptyList() else attendeeRepository.findAllByEventIdIn(listOf(matchId))

        return match.externaliseWithAttendees(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createMatch(
        @RequestBody @Valid potentialEvent: PotentialMatch,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): MatchResponse {
        log.debug("postMatch $potentialEvent")
        secretService.ensureSecret(secret)

        val allUsers = userRepository.findAll()

        val requestedUsersToAdd = allUsers.filter {
            potentialEvent.userIds?.any { a -> a == it.id }
                ?: true
        }

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException("Not all requested userIds exists unfortunately ${potentialEvent.userIds}. Please verify your userIds")
        }

        val match = potentialEvent.internalize()
        val savedMatch = matchRepository.save(match)

        val attendees = requestedUsersToAdd.map { it.toAttendee(match) }
        val savedAttendees = attendeeRepository.saveAll(attendees).toList()

        return savedMatch.externaliseWithAttendees(savedAttendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{match-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): List<Attendee> {
        log.debug("Adding: $user (or all: $addAll) to match $matchId")
        secretService.ensureSecret(secret)

        val match = matchRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        val users = if (addAll) {
            userRepository.findAll()
        } else {
            userRepository
                .findByIdOrNull(user.userId)
                ?.let(::listOf)
                ?: throw InvalidUserException(user.userId)
        }

        return users.map { u ->
            attendeeRepository.save(u.toAttendee(match))
        }
    }

    @PutMapping("/{match-id}")
    fun updateMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestBody updateMatchRequest: UpdateMatchRequest,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): MatchResponse {
        secretService.ensureSecret(secret)

        return matchRepository
            .findByIdOrNull(matchId)
            ?.let {
                val updatedMatch = it.createUpdatedMatch(updateMatchRequest)
                matchRepository.save(updatedMatch)
            }
            ?.externalise(emptyList())
            ?: throw InvalidMatchException(matchId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{match-id}")
    fun deleteMatch(
        @PathVariable(value = "match-id") matchId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ) {
        log.debug("Deleting match: $matchId")
        secretService.ensureSecret(secret)

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
