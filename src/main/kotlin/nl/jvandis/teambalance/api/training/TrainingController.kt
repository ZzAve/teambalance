package nl.jvandis.teambalance.api.training

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.toResponse
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
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
import java.time.Instant

@RestController
@Api(tags = ["trainings"])
@RequestMapping(path = ["/api/trainings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TrainingController(
    private val eventRepository: TrainingRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTrainings(
        @RequestParam(value = "includeAttendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "since", required = false) since: Instant?,
        @RequestParam(value = "limit", defaultValue = "50") limit: Int

    ): TrainingsResponse {
        log.info("GetAllTrainings")
        return eventRepository.findAll()
            .filterNotNull()
            .filter { since == null || since < it.startTime }
            .take(limit)
            .map {
                val attendees = if (includeAttendees) attendeeRepository.findAllByEventIdIn(listOf(it.id)).toTrainingResponse(it.id) else null
                TrainingResponse(
                    id = it.id,
                    comment = it.comment,
                    location = it.location,
                    startTime = it.startTime,
                    attendees = attendees

                )
            }.let(::TrainingsResponse)
    }

    @GetMapping("/{training-id}")
    fun getTraining(
        @PathVariable("training-id") trainingId: Long,
        @RequestParam(value = "includeAttendees", defaultValue = "false") includeAttendees: Boolean
    ): TrainingResponse {
        log.info("Get training $trainingId")
        val training = eventRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(trainingId)
        val attendees = if (!includeAttendees) null else attendeeRepository.findAllByEventIdIn(listOf(trainingId)).toResponse()

        return training.let {
            TrainingResponse(
                id = it.id,
                location = it.location,
                comment = it.comment,
                startTime = it.startTime,
                attendees = attendees
            )
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(@RequestBody potentialTraining: PotentialTraining): TrainingResponse {
        log.info("postTraining $potentialTraining")
        val users = userRepository.findAll()
        val training = potentialTraining.internalize()

        val attendees = users.map { it.toAttendee(training) }

        val savedTraining = eventRepository.save(training)
        val savedAttendeesResponse = attendeeRepository.saveAll(attendees).toTrainingResponse(savedTraining.id)

        return TrainingResponse(
            id = savedTraining.id,
            comment = training.comment,
            location = savedTraining.location,
            startTime = savedTraining.startTime,
            attendees = savedAttendeesResponse
        )
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{training-id}/attendee")
    fun addAttendee(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest
    ): List<Attendee> {
        log.info("Adding: $user (or all: $addAll) to training $trainingId")

        val training = eventRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(trainingId)
        val users = if (addAll) {
            userRepository.findAll()
        } else {
            userRepository
                .findByIdOrNull(user.userId)
                ?.let(::listOf)
                ?: throw InvalidUserException(user.userId)
        }

        return users.map { u ->
            attendeeRepository.save(u.toAttendee(training))
        }
    }

    @PutMapping("/{training-id}")
    fun updateTraining(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestBody updateTrainingRequest: UpdateTrainingRequest

    ): Training {
        return eventRepository
            .findByIdOrNull(trainingId)
            ?.let { if (it is Training) it else null }
            ?.let {
                val updatedTraining = it.createUpdatedTraining(updateTrainingRequest)
                eventRepository.save(updatedTraining)
            } ?: throw InvalidTrainingException(trainingId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{training-id}")
    fun deleteTraining(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "deleteAttendees", defaultValue = "false") deleteAttendees: Boolean
    ) {
        log.info("Deleting training: $trainingId")

        if (deleteAttendees) {
            attendeeRepository.findAllByEventIdIn(listOf(trainingId))
                .let { attendeeRepository.deleteAll(it) }
        }
        try {
            eventRepository.deleteById(trainingId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Training $trainingId could not be deleted. There are still attendees bound to this training")
        }
    }

    private fun Training.createUpdatedTraining(updateTrainingRequestBody: UpdateTrainingRequest) = copy(
        startTime = updateTrainingRequestBody.startTime?.let(Instant::ofEpochMilli) ?: startTime,
        comment = updateTrainingRequestBody.comment ?: comment,
        location = updateTrainingRequestBody.location ?: location
    )
}
