package nl.jvandis.teambalance.api.attendees

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.InvalidAttendeeException
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
        @RequestParam(value = "event-ids", defaultValue = "") eventIds: List<Long>,
        @RequestParam(value = "user-ids", defaultValue = "") userIds: List<Long>,
    ): AttendeesResponse {
        log.debug("Get attendees (filter eventIds: $eventIds,userIds: $userIds")

        val attendees =
            when {
                eventIds.isEmpty() && userIds.isEmpty() -> attendeeRepository.findAll()
                eventIds.isNotEmpty() && userIds.isNotEmpty() ->
                    attendeeRepository.findALlByEventIdInAndUserIdIn(
                        eventIds,
                        userIds,
                    )

                eventIds.isNotEmpty() -> attendeeRepository.findAllByEventIdIn(eventIds)
                else -> attendeeRepository.findAllByUserIdIn(userIds)
            }

        return attendees
            .map(Attendee::expose)
            .let(::AttendeesResponse)
    }

    @GetMapping("/{id}")
    fun getAttendee(
        @PathVariable(value = "id") attendeeId: Long,
    ): AttendeeResponse {
        log.debug("Get attendees $attendeeId")

        val attendee = attendeeRepository.findByIdOrNull(attendeeId) ?: throw InvalidAttendeeException(attendeeId)

        return attendee.expose()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun addAttendee(
        @RequestBody potentialAttendee: PotentialAttendee,
    ): AttendeeResponse {
        log.debug("Adding attendee: $potentialAttendee")

        val user =
            userRepository.findByIdOrNull(potentialAttendee.userId)
                ?: throw InvalidUserException(potentialAttendee.userId)

        if (!eventRepository.exists(potentialAttendee.eventId)) {
            throw InvalidTrainingException(potentialAttendee.eventId)
        }

        return try {
            attendeeRepository.insert(
                Attendee(
                    user = user,
                    eventId = potentialAttendee.eventId,
                    availability = potentialAttendee.state,
                ),
            ).expose()
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException(
                "Could not add user ${potentialAttendee.userId} to training ${potentialAttendee.eventId}. User already added",
            )
        }
    }

    @Deprecated(
        "Superseded by PUT api/attendees/{id}/availability",
        replaceWith = ReplaceWith("updateAttendeeAvailability"),
    )
    @PutMapping("{id}")
    fun updateAttendee(
        @PathVariable("id") attendeeId: Long,
        @RequestBody attendeeStateUpdate: AttendeeStateUpdate,
    ): AttendeeResponse {
        return updateAttendeeAvailability(attendeeId, attendeeStateUpdate)
    }

    @PutMapping("{id}/availability")
    fun updateAttendeeAvailability(
        @PathVariable("id") attendeeId: Long,
        @RequestBody attendeeStateUpdate: AttendeeStateUpdate,
    ): AttendeeResponse {
        val success =
            attendeeRepository
                .updateAvailability(attendeeId, attendeeStateUpdate.availability)

        if (!success) {
            throw IllegalStateException("Could not update Attendee state with id $attendeeId.")
        }
        return attendeeRepository.findByIdOrNull(attendeeId)
            ?.expose()
            ?: throw AttendeeNotFoundException(attendeeId, -1L)
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
    @DeleteMapping()
    fun deleteAttendeeByUserIdAndEventId(
        @RequestParam("user-id") userId: Long,
        @RequestParam("event-id") eventId: Long,
    ) {
        log.debug("Deleting user $userId from training $eventId")

        attendeeRepository.findByUserIdAndEventId(userId, eventId)
            .firstOrNull()
            ?.let {
                attendeeRepository.delete(it)
            }
            ?: throw AttendeeNotFoundException(userId, eventId)
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

data class AttendeeNotFoundException(val userId: Long, val eventId: Long) : RuntimeException()

data class PotentialAttendee(
    val eventId: Long,
    val userId: Long,
    val state: Availability = Availability.NOT_RESPONDED,
)

data class AttendeeStateUpdate(
    val availability: Availability,
)
