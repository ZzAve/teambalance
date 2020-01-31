package nl.jvandis.teambalance.api.attendees

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.training.TrainingRepository
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
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
        private val trainingRepository: TrainingRepository,
        private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAttendees(
            @RequestParam(value = "trainingIds", defaultValue = "") trainingIds: List<Long>,
            @RequestParam(value = "userIds", defaultValue = "") userIds: List<Long>
    ): AttendeesResponse {
        log.info("Get attendees (filter trainingIds: $trainingIds,userIds: $userIds")

        val attendees = when {
            trainingIds.isEmpty() && userIds.isEmpty() -> attendeeRepository.findAll()
            trainingIds.isNotEmpty() && userIds.isNotEmpty() -> attendeeRepository.findALlByTrainingIdInAndUserIdIn(trainingIds,userIds)
            trainingIds.isNotEmpty() -> attendeeRepository.findAllByTrainingIdIn(trainingIds)
            else -> attendeeRepository.findAllByUserIdIn(userIds)
        }

        return attendees
                .filterNotNull()
                .map {
                    AttendeeResponse(
                            id = it.id,
                            trainingId = it.training.id,
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

        val training = trainingRepository.findByIdOrNull(potentialAttendee.trainingId)
                ?: throw InvalidTrainingException(potentialAttendee.trainingId)

        return attendeeRepository.save(Attendee(
                user = user,
                training = training,
                availability = potentialAttendee.state
        ))


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
        val trainingId : Long,
        val userId: Long,
        val state: Availability
)

data class AttendeeStateUpdate(
        val availability: Availability
)
