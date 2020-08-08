package nl.jvandis.teambalance.api.attendees

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.InvalidAttendeeException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import nl.jvandis.teambalance.api.training.EventRepository
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["attendees"])
@RequestMapping(path = ["/api/attendees"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AttendeeController(
    private val attendeeRepository: AttendeeRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val secretService: SecretService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAttendees(
        @RequestParam(value = "event-ids", defaultValue = "") eventIds: List<Long>,
        @RequestParam(value = "user-ids", defaultValue = "") userIds: List<Long>,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): AttendeesResponse {
        log.info("Get attendees (filter eventIds: $eventIds,userIds: $userIds")
        secretService.ensureSecret(secret)

        val attendees = when {
            eventIds.isEmpty() && userIds.isEmpty() -> attendeeRepository.findAll()
            eventIds.isNotEmpty() && userIds.isNotEmpty() -> attendeeRepository.findALlByEventIdInAndUserIdIn(
                eventIds,
                userIds
            )
            eventIds.isNotEmpty() -> attendeeRepository.findAllByEventIdIn(eventIds)
            else -> attendeeRepository.findAllByUserIdIn(userIds)
        }

        return attendees
            .filterNotNull()
            .map { it.toResponse() }
            .let(::AttendeesResponse)
    }

    @GetMapping("/{id}")
    fun getAttendee(
        @PathVariable(value = "id") attendeeId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): AttendeeResponse {
        log.info("Get attendees $attendeeId")
        secretService.ensureSecret(secret)

        val attendee = attendeeRepository.findByIdOrNull(attendeeId) ?: throw InvalidAttendeeException(attendeeId)

        return attendee.toResponse()
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun addAttendee(@RequestBody potentialAttendee: PotentialAttendee,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?): AttendeeResponse {
        log.info("Adding attendee: $potentialAttendee")
        secretService.ensureSecret(secret)

        val user = userRepository.findByIdOrNull(potentialAttendee.userId)
            ?: throw InvalidUserException(potentialAttendee.userId)

        val event = eventRepository.findByIdOrNull(potentialAttendee.eventId)
            ?: throw InvalidTrainingException(potentialAttendee.eventId)

        return try {
            attendeeRepository.save(
                Attendee(
                    user = user,
                    event = event,
                    availability = potentialAttendee.state
                )
            ).toResponse()
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Could not add user ${potentialAttendee.userId} to training ${potentialAttendee.eventId}. User already added")
        }
    }

    @PutMapping("{id}")
    fun updateAttendee(
        @PathVariable("id") attendeeId: Long,
        @RequestBody attendeeStateUpdate: AttendeeStateUpdate,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): AttendeeResponse {
        secretService.ensureSecret(secret)

        val attendee = attendeeRepository.findByIdOrNull(attendeeId)
            ?: throw InvalidAttendeeException(attendeeId)

        val updatedAttendee = attendee.let {
            attendeeRepository.save(it.copy(availability = attendeeStateUpdate.availability))
        }

        return updatedAttendee.toResponse()
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteAttendee(
        @PathVariable("id") id: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.info("Deleting attendee x")
        secretService.ensureSecret(secret)

        attendeeRepository.deleteById(id)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping()
    fun deleteAttendeeByUserIdAndEventId(
        @RequestParam("user-id") userId: Long,
        @RequestParam("event-id") eventId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.info("Deleting user $userId from training $eventId")
        secretService.ensureSecret(secret)

        val attendee =
            attendeeRepository.findByUserIdAndEventId(userId, eventId)
                .firstOrNull()
                ?.let {

                    log.info(it.toString())
                    attendeeRepository.delete(it)
                    // attendeeRepository.deleteById(userId)
                }
                ?: throw AttendeeNotFoundException(userId, eventId)
    }

    private fun Attendee.toResponse() =
        AttendeeResponse(
            id = id,
            eventId = event.id,
            user = user,
            state = availability
        )

    @ExceptionHandler(AttendeeNotFoundException::class)
    fun attendeeNotFoundException(e: AttendeeNotFoundException) = ResponseEntity.status(NOT_FOUND)
        .body(
            Error(
                status = NOT_FOUND,
                reason ="Could not find attendee for user ${e.userId} on event ${e.eventId}"
            )
        )
}

data class AttendeeNotFoundException(val userId: Long, val eventId: Long) : RuntimeException()

data class PotentialAttendee(
    val eventId: Long,
    val userId: Long,
    val state: Availability = Availability.NOT_RESPONDED
)

data class AttendeeStateUpdate(
    val availability: Availability
)
