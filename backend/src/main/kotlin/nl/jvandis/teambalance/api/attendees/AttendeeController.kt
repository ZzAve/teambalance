package nl.jvandis.teambalance.api.attendees

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.InvalidAttendeeException
import nl.jvandis.teambalance.api.InvalidEventException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.event.EventRepository
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "attendees")
@RequestMapping(path = ["/api/attendees"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AttendeeController(
    private val attendeeRepository: AttendeeRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAttendees(
        @RequestParam(value = "event-ids", defaultValue = "") eventIds: List<String>,
        @RequestParam(value = "user-ids", defaultValue = "") userIds: List<String>,
    ): AttendeesResponse {
        val eventTeamBalanceIds = eventIds.map(TeamBalanceId::invoke)
        val userTeamBalanceIds = userIds.map(TeamBalanceId::invoke)
        log.debug("Get attendees (filter eventIds: {},userIds: {}", eventTeamBalanceIds, userTeamBalanceIds)

        val attendees =
            when {
                eventTeamBalanceIds.isEmpty() && userTeamBalanceIds.isEmpty() -> attendeeRepository.findAll()
                eventTeamBalanceIds.isNotEmpty() && userTeamBalanceIds.isNotEmpty() ->
                    attendeeRepository.findALlByEventIdInAndUserIdIn(
                        eventTeamBalanceIds,
                        userTeamBalanceIds,
                    )

                eventTeamBalanceIds.isNotEmpty() -> attendeeRepository.findAllByEventIdIn(eventTeamBalanceIds)
                else -> attendeeRepository.findAllByUserIdIn(userTeamBalanceIds)
            }

        return attendees
            .map(Attendee::expose)
            .let(::AttendeesResponse)
    }

    @GetMapping("/{id}")
    fun getAttendee(
        @PathVariable(value = "id") attendeeId: String,
    ): AttendeeResponse {
        val attendeeTeamBalanceId = TeamBalanceId(attendeeId)
        log.debug("Get attendees $attendeeTeamBalanceId")

        val attendee =
            attendeeRepository.findByIdOrNull(attendeeTeamBalanceId) ?: throw InvalidAttendeeException(
                attendeeTeamBalanceId,
            )

        return attendee.expose()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun addAttendee(
        @RequestBody potentialAttendee: PotentialAttendee,
    ): AttendeeResponse {
        log.debug("Adding attendee: {}", potentialAttendee)

        val userTeamBalanceId = TeamBalanceId(potentialAttendee.userId)
        val user =
            userRepository.findByIdOrNull(userTeamBalanceId)
                ?: throw InvalidUserException(userTeamBalanceId)

        val eventTeamBalanceId = TeamBalanceId(potentialAttendee.eventId)
        if (!eventRepository.exists(eventTeamBalanceId)) {
            throw InvalidTrainingException(eventTeamBalanceId)
        }
        val eventInternalId = eventRepository.findInternalId(eventTeamBalanceId) ?: throw InvalidEventException(eventTeamBalanceId)
        return try {
            attendeeRepository.insert(
                eventInternalId = eventInternalId,
                user = user,
                availability = potentialAttendee.state,
            ).expose()
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException(
                "Could not add user $userTeamBalanceId to training $eventTeamBalanceId. User already added",
            )
        }
    }

    @Deprecated(
        "Superseded by PUT api/attendees/{id}/availability",
        replaceWith = ReplaceWith("updateAttendeeAvailability"),
    )
    @PutMapping("{id}")
    fun updateAttendee(
        @PathVariable("id") attendeeId: String,
        @RequestBody attendeeStateUpdate: AttendeeStateUpdate,
    ): AttendeeResponse = updateAttendeeAvailability(attendeeId, attendeeStateUpdate)

    @PutMapping("{id}/availability")
    fun updateAttendeeAvailability(
        @PathVariable("id") attendeeId: String,
        @RequestBody attendeeStateUpdate: AttendeeStateUpdate,
    ): AttendeeResponse {
        val attendeeTeamBalanceId = TeamBalanceId(attendeeId)
        val success =
            attendeeRepository
                .updateAvailability(attendeeTeamBalanceId, attendeeStateUpdate.availability)

        if (!success) {
            throw IllegalStateException("Could not update Attendee state with id $attendeeTeamBalanceId.")
        }
        return attendeeRepository.findByIdOrNull(attendeeTeamBalanceId)
            ?.expose()
            ?: throw AttendeeNotFoundException(attendeeTeamBalanceId, TeamBalanceId("unknown user"))
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteAttendee(
        @PathVariable id: Long,
    ) {
        log.debug("Deleting attendee x")

        attendeeRepository.deleteById(id)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping
    fun deleteAttendeeByUserIdAndEventId(
        @RequestParam("user-id") userId: String,
        @RequestParam("event-id") eventId: String,
    ) {
        val userTeamBalanceId = TeamBalanceId(userId)
        val eventTeamBalanceId = TeamBalanceId(eventId)
        log.debug("Deleting user $userTeamBalanceId from training $eventTeamBalanceId")
        attendeeRepository.findByUserIdAndEventId(userTeamBalanceId, eventTeamBalanceId)
            .firstOrNull()
            ?.let {
                attendeeRepository.delete(it)
            }
            ?: throw AttendeeNotFoundException(userTeamBalanceId, eventTeamBalanceId)
    }

    @ExceptionHandler(AttendeeNotFoundException::class)
    fun attendeeNotFoundException(e: AttendeeNotFoundException) =
        ResponseEntity.status(NOT_FOUND)
            .body(
                Error(
                    status = NOT_FOUND,
                    reason = "Could not find attendee for user ${e.userId} on event ${e.eventId}",
                ),
            )
}

data class AttendeeNotFoundException(val userId: TeamBalanceId, val eventId: TeamBalanceId) : RuntimeException()

data class PotentialAttendee(
    val eventId: String,
    val userId: String,
    val state: Availability = Availability.NOT_RESPONDED,
)

data class AttendeeStateUpdate(
    val availability: Availability,
)
