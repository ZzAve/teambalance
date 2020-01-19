package nl.jvandis.teambalance.api.attendees

import io.swagger.annotations.Api
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Api(tags = ["attendees"])
@RequestMapping(path = ["/api/attendees"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AttendeeController(
        private val attendeeRepository: AttendeeRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAttendees(
            @RequestParam(value = "trainingIds", required = false) trainingIds: List<Long>?,
            @RequestParam(value = "userIds", required = false) userIds: List<Long>?
    ): AttendeesResponse {
        log.info("Get attendees (filter trainingIds: $trainingIds,userIds: $userIds")

        val attendees = when {
            trainingIds == null && userIds == null -> attendeeRepository.findAll()
            trainingIds != null && userIds != null -> attendeeRepository.findALlByTrainingIdInAndUserIdIn(trainingIds, userIds)
            trainingIds != null -> attendeeRepository.findAllByTrainingIdIn(trainingIds)
            else -> attendeeRepository.findAllByUserIdIn(userIds)
        }
        return attendees
                .filterNotNull()
                .map {
                    AttendeeResponse(
                            id = it.id,
                            trainingId = it.training.id,
                            user = it.user,
                            state = it.state
                    )
                }.let(::AttendeesResponse)
    }

    @PutMapping("{id}")
    fun updateAttendee(
            @PathVariable("id") id: Long,
            @RequestBody attendeeStateUpdate: AttendeeStateUpdate
    ) {
        attendeeRepository.findById(id).map {
            attendeeRepository.save(it.copy(state = attendeeStateUpdate.state))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAttendee(@PathVariable("id") id: Long
    ) {
        log.info("Deleting attendee x")
        attendeeRepository.deleteById(id)
    }


}

data class AttendeeStateUpdate(
        val state: Availability
)
