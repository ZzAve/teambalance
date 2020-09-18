package nl.jvandis.teambalance.api.attendees

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.Status
import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.ErrorResponse
import nl.jvandis.teambalance.api.InvalidAttendeeException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import nl.jvandis.teambalance.api.event.EventRepository
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import java.io.IOException

@Tag(name = "attendees")
@Controller("/api/attendees", produces = [MediaType.APPLICATION_JSON])
class AttendeeController(
    private val attendeeRepository: AttendeeRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val secretService: SecretService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Get
    fun getAttendees(
        @QueryValue(value = "event-ids", defaultValue = "") eventIds: List<Long>,
        @QueryValue(value = "user-ids", defaultValue = "") userIds: List<Long>,
        @Header(value = SECRET_HEADER) secret: String?
    ): AttendeesResponse {
        log.debug("Get attendees (filter eventIds: $eventIds,userIds: $userIds")
        secretService.ensureSecret(secret)

        val attendees = when {
            eventIds.isEmpty() && userIds.isEmpty() -> attendeeRepository.findAll()
            eventIds.isNotEmpty() && userIds.isNotEmpty() -> attendeeRepository.findAllByEventIdInAndUserIdIn(
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

    @Get("/{id}")
    fun getAttendee(
        @PathVariable(value = "id") attendeeId: Long,
        @Header(value = SECRET_HEADER) secret: String?
    ): AttendeeResponse {
        log.debug("Get attendees $attendeeId")
        secretService.ensureSecret(secret)

        val attendee = attendeeRepository
            .findById(attendeeId)
            .orElseThrow { InvalidAttendeeException(attendeeId) }

        return attendee.toResponse()
    }

    @Status(HttpStatus.CREATED)
    @Post
    fun addAttendee(
        @Body potentialAttendee: PotentialAttendee,
        @Header(value = SECRET_HEADER) secret: String?
    ): AttendeeResponse {
        log.debug("Adding attendee: $potentialAttendee")
        secretService.ensureSecret(secret)

        val user = userRepository
            .findById(potentialAttendee.userId)
            .orElseThrow { InvalidUserException(potentialAttendee.userId) }

        val event = eventRepository
            .findById(potentialAttendee.eventId)
            .orElseThrow { InvalidTrainingException(potentialAttendee.eventId) }

        return try {
            attendeeRepository.save(
                Attendee(
                    user = user,
                    event = event,
                    availability = potentialAttendee.state
                )
            ).toResponse()
        } catch (e: IOException) { // FIXME: find right exception to catch
            throw DataConstraintViolationException("Could not add user ${potentialAttendee.userId} to training ${potentialAttendee.eventId}. User already added")
        }
    }

    @Put("{id}")
    fun updateAttendee(
        @PathVariable("id") attendeeId: Long,
        @Body attendeeStateUpdate: AttendeeStateUpdate,
        @Header(value = SECRET_HEADER) secret: String?
    ): AttendeeResponse {
        secretService.ensureSecret(secret)

        val attendee = attendeeRepository.findById(attendeeId)
            .orElseThrow { InvalidAttendeeException(attendeeId) }

        val updatedAttendee = attendee.let {
            attendeeRepository.save(it.copy(availability = attendeeStateUpdate.availability))
        }

        return updatedAttendee.toResponse()
    }

    @Status(HttpStatus.NO_CONTENT)
    @Delete("/{id}")
    fun deleteAttendee(
        @PathVariable("id") id: Long,
        @Header(value = SECRET_HEADER) secret: String?
    ) {
        log.debug("Deleting attendee x")
        secretService.ensureSecret(secret)

        attendeeRepository.deleteById(id)
    }

    @Status(HttpStatus.NO_CONTENT)
    @Delete()
    fun deleteAttendeeByUserIdAndEventId(
        @QueryValue("user-id") userId: Long,
        @QueryValue("event-id") eventId: Long,
        @Header(value = SECRET_HEADER) secret: String?
    ) {
        log.debug("Deleting user $userId from training $eventId")
        secretService.ensureSecret(secret)

        attendeeRepository.findByUserIdAndEventId(userId, eventId)
            .firstOrNull()
            ?.let {
                attendeeRepository.delete(it)
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

    @Error(AttendeeNotFoundException::class)
    fun attendeeNotFoundException(e: AttendeeNotFoundException): HttpResponse<ErrorResponse> =
        HttpResponse.status<ErrorResponse>(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND,
                    reason = "Could not find attendee for user ${e.userId} on event ${e.eventId}"
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
