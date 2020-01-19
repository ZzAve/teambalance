package nl.jvandis.teambalance.api.training

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.*
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@Api(tags = ["trainings"])
@RequestMapping(path = ["/api/trainings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TrainingController(
        private val trainingRepository: TrainingRepository,
        private val userRepository: UserRepository,
        private val attendeeRepository: AttendeeRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTrainings(
            @RequestParam(value = "includeAttendees", defaultValue = "false") includeAttendees: Boolean
    ): TrainingsResponse {
        log.info("GetAllTrainings")
        return trainingRepository.findAll()
                .filterNotNull()
                .map {
                    val attendees = if (includeAttendees) attendeeRepository.findAllByTrainingIdIn(listOf(it.id)).toTrainingResponse(it.id) else null
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
    fun getTraining(@PathVariable("training-id") trainingId: Long,
                    @RequestParam(value = "includeAttendees", defaultValue = "false") includeAttendees: Boolean
    ): TrainingResponse {
        log.info("Get training $trainingId")
        val training = trainingRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(trainingId)
        val attendees = if (!includeAttendees) null else attendeeRepository.findAllByTrainingIdIn(listOf(trainingId)).toResponse()

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
    fun postUser(@RequestBody potentialTraining: PotentialTraining): Training {
        log.info("postTraining $potentialTraining")
        val users = userRepository.findAll()
        val training = potentialTraining.internalize(users)
        return trainingRepository.save(training)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{training-id}/attendee")
    fun addAttendee(
            @PathVariable(value = "training-id") trainingId: Long,
            @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
            @RequestBody user: UserAddRequest
    ): List<Attendee> {
        log.info("Adding: $user to training $trainingId")

        return trainingRepository.findById(trainingId).map { training ->
            val users = if (addAll) {
                userRepository.findAll()
            } else {
                userRepository
                        .findById(user.userId)
                        .map(::listOf)
                        .orElseThrow {
                            InvalidUserException(user.userId)
                        }
            }

            users.map { u ->
                attendeeRepository.save(u.toAttendee(training))
            }
        }.orElseThrow { InvalidTrainingException(trainingId) }
    }

    @PutMapping("/{training-id}")
    fun updateTraining(
            @PathVariable(value = "training-id") trainingId: Long,
            @RequestBody updateTrainingRequest: UpdateTrainingRequest

    ): Training {
        return trainingRepository
                .findByIdOrNull(trainingId)
                ?.let {
                    val updatedTraining = it.createUpdatedTraining(updateTrainingRequest)
                    trainingRepository.save(updatedTraining)
                } ?: throw InvalidTrainingException(trainingId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{training-id}")
    fun updateUser(
            @PathVariable(value = "training-id") trainingId: Long
    ) {
        log.info("Deleting training: $trainingId")
        trainingRepository.deleteById(trainingId)
    }

    private fun Training.createUpdatedTraining(updateTrainingRequestBody: UpdateTrainingRequest) = copy(
            startTime = updateTrainingRequestBody.startTime?.let(Instant::ofEpochMilli) ?: startTime,
            comment = updateTrainingRequestBody.comment ?: comment,
            location = updateTrainingRequestBody.location ?: location
    )
}
