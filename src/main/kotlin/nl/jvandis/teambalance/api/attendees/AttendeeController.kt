package nl.jvandis.teambalance.api.attendees

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.training.EventRepository
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Api(tags = ["attendees"])
@RequestMapping(path = ["/api/attendees"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AttendeeController(
        private val attendeeRepository: AttendeeRepository,
        private val eventRepository: EventRepository,
        private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAttendees(
            @RequestParam(value = "eventIds", defaultValue = "") eventIds: List<Long>,
            @RequestParam(value = "userIds", defaultValue = "") userIds: List<Long>
    ): AttendeesResponse {
        log.info("Get attendees (filter eventIds: $eventIds,userIds: $userIds")

        val attendees = when {
            eventIds.isEmpty() && userIds.isEmpty() -> attendeeRepository.findAll()
            eventIds.isNotEmpty() && userIds.isNotEmpty() -> attendeeRepository.findALlByEventIdInAndUserIdIn(eventIds,userIds)
            eventIds.isNotEmpty() -> attendeeRepository.findAllByEventIdIn(eventIds)
            else -> attendeeRepository.findAllByUserIdIn(userIds)
        }

        return attendees
                .filterNotNull()
                .map {
                    AttendeeResponse(
                            id = it.id,
                            eventId = it.event.id,
                            user = it.user,
                            state = it.availability
                    )
                }.let(::AttendeesResponse)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun addAttendee(@RequestBody potentialAttendee: PotentialAttendee ): Attendee {
        log.info("Adding attendee: $potentialAttendee")

        val user = userRepository.findByIdOrNull(potentialAttendee.userId) ?:
                throw InvalidUserException(potentialAttendee.userId)

        val event = eventRepository.findByIdOrNull(potentialAttendee.eventId)
                ?: throw InvalidTrainingException(potentialAttendee.eventId)

        return try {
            attendeeRepository.save(Attendee(
                    user = user,
                    event = event,
                    availability = potentialAttendee.state
            ))
        } catch (e : DataIntegrityViolationException){
            throw DataConstraintViolationException("Could not add user ${potentialAttendee.userId} to training ${potentialAttendee.eventId}. User already added")
        }
    }

    @PutMapping("{id}")
    fun updateAttendee(
            @PathVariable("id") id: Long,
            @RequestBody attendeeStateUpdate: AttendeeStateUpdate
    ) {
        attendeeRepository.findById(id).map {
            attendeeRepository.save(it.copy(availability = attendeeStateUpdate.availability))
        }
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteAttendee(@PathVariable("id") id: Long
    ) {
        log.info("Deleting attendee x")
        attendeeRepository.deleteById(id)
    }


}

data class PotentialAttendee (
        val eventId : Long,
        val userId: Long,
        val state: Availability
)

data class AttendeeStateUpdate(
        val availability: Availability
)
