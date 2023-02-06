package nl.jvandis.teambalance.api.training

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.toResponse
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toNewAttendee
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.math.min

@RestController
@Tag(name = "trainings")
@RequestMapping(path = ["/api/trainings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TrainingController(
    private val eventRepository: TrainingRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTrainings(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(
            value = "since",
            defaultValue = "2022-08-01T00:00"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        since: LocalDateTime,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        @RequestParam(value = "page", defaultValue = "1") page: Int
    ): TrainingsResponse {
        log.debug("GetAllTrainings")
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
            page = page,
            limit = limit,
            since = since,
            includeAttendees = includeAttendees
        ).toResponse(includeInactiveUsers)
    }

    private fun Page<Training>.toResponse(includeInactiveUsers: Boolean) = TrainingsResponse(
        totalPages = totalPages,
        totalSize = totalElements,
        page = number + 1,
        size = min(size, content.size),
        trainings = content.map { it.expose(includeInactiveUsers) }
    )

    @GetMapping("/{training-id}")
    fun getTraining(
        @PathVariable("training-id") trainingId: Long,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean

    ): TrainingResponse {
        log.debug("Get training $trainingId")
        val training = eventRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(trainingId)
        val attendees =
            if (!includeAttendees) {
                emptyList()
            } else {
                attendeeRepository
                    .findAllByEventIdIn(listOf(trainingId))
                    .filter { a -> a.user.isActive || includeInactiveUsers }
            }

        return training.expose(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createTraining(
        @RequestBody potentialEvent: PotentialTraining
    ): TrainingResponse {
        log.debug("postTraining $potentialEvent")
        val allUsers = userRepository.findAll()
        val training = potentialEvent.internalize()

        val requestedUsersToAdd = allUsers.filter { user ->
            potentialEvent.userIds?.any { it == user.id }
                ?: true
        }

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException("Not all requested userIds exists unfortunately ${potentialEvent.userIds}. Please verify your userIds")
        }

        val savedTraining = eventRepository.insert(training)
        val attendees = requestedUsersToAdd.map { it.toNewAttendee(savedTraining) }
        val savedAttendees = attendeeRepository.insertMany(attendees)

        return savedTraining.expose(savedAttendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{training-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest

    ): List<AttendeeResponse> {
        log.debug("Adding: $user (or all: $addAll) to training $trainingId")
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
            attendeeRepository.insert(u.toNewAttendee(training)).toResponse()
        }
    }

    @PutMapping("/{training-id}")
    fun updateTraining(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestBody updateTrainingRequest: UpdateTrainingRequest
    ): TrainingResponse {
        return eventRepository
            .findByIdOrNull(trainingId)
            ?.let {
                val updatedTraining = it.createUpdatedTraining(updateTrainingRequest)
                eventRepository.update(updatedTraining)
            }
            ?.expose(emptyList())
            ?: throw InvalidTrainingException(trainingId)
    }

    @PutMapping("/{training-id}/trainer")
    fun updateTrainer(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestBody updateTrainerRequest: UpdateTrainerRequest
    ): TrainingResponse {
        return eventRepository
            .findByIdOrNull(trainingId)
            ?.let {
                val potentialTrainer = getPotentialTrainer(updateTrainerRequest)
                val updatedTraining = it.copy(
                    trainer = potentialTrainer
                )
                eventRepository.update(updatedTraining)
            }
            ?.expose(emptyList())
            ?: throw InvalidTrainingException(trainingId)
    }

    private fun getPotentialTrainer(updateTrainerRequest: UpdateTrainerRequest): User? {
        return when (updateTrainerRequest.userId) {
            null -> null
            else ->
                userRepository
                    .findByIdOrNull(updateTrainerRequest.userId)
                    ?: throw InvalidUserException(updateTrainerRequest.userId)
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{training-id}")
    fun deleteTraining(
        @PathVariable(value = "training-id") trainingId: Long,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean

    ) {
        log.debug("Deleting training: $trainingId")
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