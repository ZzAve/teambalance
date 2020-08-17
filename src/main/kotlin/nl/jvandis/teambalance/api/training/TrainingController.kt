package nl.jvandis.teambalance.api.training

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
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
import kotlin.math.min

@RestController
@Api(tags = ["trainings"])
@RequestMapping(path = ["/api/trainings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TrainingController(
    private val eventRepository: TrainingRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
    private val secretService: SecretService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTrainings(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(
            value = "since",
            defaultValue = "2020-09-01T00:00"
        ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: LocalDateTime,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        @RequestParam(value = "page", defaultValue = "1") page: Int,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): TrainingsResponse {
        log.info("GetAllTrainings")
        secretService.ensureSecret(secret)

        // In case of testing performance again :)
        // measureTiming(50) {
        //     getEventsAndAttendees(
        //         repository = eventRepository,
        //         attendeeRepository = attendeeRepository,
        //         page = page,
        //         limit = limit,
        //         since = since,
        //         includeAttendees = includeAttendees
        //     ).toResponse()
        // }

        return getEventsAndAttendees(
            eventsRepository = eventRepository,
            attendeeRepository = attendeeRepository,
            page = page,
            limit = limit,
            since = since,
            includeAttendees = includeAttendees
        ).toResponse()
    }

    private fun Pair<Page<Training>, Map<Long, List<Attendee>>>.toResponse(
    ): TrainingsResponse {
        val trainingsPage = first
        val attendees = second

        return TrainingsResponse(
            totalPages = trainingsPage.totalPages,
            totalSize = trainingsPage.totalElements,
            page = trainingsPage.number + 1,
            size = min(trainingsPage.size, trainingsPage.content.size),
            trainings = trainingsPage.content.map { t ->
                val relevantAttendees = attendees[t.id] ?: emptyList()
                t.externalizeWithAttendees(relevantAttendees)
            }
        )
    }

    @GetMapping("/{training-id}")
    fun getTraining(
        @PathVariable("training-id") trainingId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): TrainingResponse {
        log.info("Get training $trainingId")
        secretService.ensureSecret(secret)

        val training = eventRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(trainingId)
        val attendees =
            if (!includeAttendees) emptyList() else attendeeRepository.findAllByEventIdIn(listOf(trainingId))

        return training.externalizeWithAttendees(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createTraining(
        @RequestBody potentialTraining: PotentialTraining,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): TrainingResponse {
        log.info("postTraining $potentialTraining")
        secretService.ensureSecret(secret)

        val users = userRepository.findAll()
        val training = potentialTraining.internalize()

        val attendees = users.map { it.toAttendee(training) }

        val savedTraining = eventRepository.save(training)
        val savedAttendees = attendeeRepository.saveAll(attendees).toList()

        return savedTraining.externalizeWithAttendees(savedAttendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{training-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): List<Attendee> {
        log.info("Adding: $user (or all: $addAll) to training $trainingId")
        secretService.ensureSecret(secret)

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
        @RequestBody updateTrainingRequest: UpdateTrainingRequest,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): TrainingResponse {
        secretService.ensureSecret(secret)

        return eventRepository
            .findByIdOrNull(trainingId)
            ?.let {
                val updatedTraining = it.createUpdatedTraining(updateTrainingRequest)
                eventRepository.save(updatedTraining)
            }
            ?.externalizeWithAttendees(emptyList())
            ?: throw InvalidTrainingException(trainingId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{training-id}")
    fun deleteTraining(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ) {
        log.info("Deleting training: $trainingId")
        secretService.ensureSecret(secret)

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
}
