package nl.jvandis.teambalance.api.event.training

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.CreateEventException
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.DeletedEventsResponse
import nl.jvandis.teambalance.api.event.EventsResponse
import nl.jvandis.teambalance.api.event.UserAddRequest
import nl.jvandis.teambalance.api.event.getEventsAndAttendees
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import nl.jvandis.teambalance.api.users.toBeInsertedAttendee
import nl.jvandis.teambalance.filters.START_OF_SEASON_RAW
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
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
    private val trainingService: TrainingService,
    private val eventRepository: TrainingRepository,
    private val userRepository: UserRepository,
    private val attendeeRepository: AttendeeRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getTrainings(
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestParam(defaultValue = START_OF_SEASON_RAW)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        since: LocalDateTime,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "1") page: Int,
    ): EventsResponse<TrainingResponse> {
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
        ).expose(includeInactiveUsers)
    }

    private fun Page<Training>.expose(includeInactiveUsers: Boolean) =
        EventsResponse(
            totalPages = totalPages,
            totalSize = totalElements,
            page = number + 1,
            size = min(size, content.size),
            events = content.map { it.expose(includeInactiveUsers) },
        )

    @GetMapping("/{training-id}")
    fun getTraining(
        @PathVariable("training-id") trainingId: String,
        @RequestParam(value = "include-attendees", defaultValue = "false") includeAttendees: Boolean,
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
    ): TrainingResponse {
        val trainingTeamBalanceId = TeamBalanceId(trainingId)
        log.debug("Get training $trainingTeamBalanceId")
        val training =
            eventRepository.findByIdOrNull(trainingTeamBalanceId) ?: throw InvalidTrainingException(
                trainingTeamBalanceId,
            )
        val attendees =
            if (!includeAttendees) {
                emptyList()
            } else {
                attendeeRepository
                    .findAllByEventIdIn(listOf(trainingTeamBalanceId))
                    .filter { a -> a.user.isActive || includeInactiveUsers }
            }

        return training.expose(attendees)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createTraining(
        @RequestBody potentialEvent: PotentialTraining,
    ): EventsResponse<TrainingResponse> {
        log.debug("postTraining {}", potentialEvent)
        val allUsers = userRepository.findAll()
        val events = potentialEvent.internalize()
        log.info("Requested to create trainings events: $events")

        val requestedUsersToAdd =
            allUsers.filter { user ->
                potentialEvent.userIds?.any { it == user.teamBalanceId.value }
                    ?: true
            }
        log.info("Users to add to training events: $requestedUsersToAdd")

        if (potentialEvent.userIds != null &&
            potentialEvent.userIds.size != requestedUsersToAdd.size
        ) {
            throw CreateEventException(
                "Not all requested userIds exists unfortunately ${potentialEvent.userIds}." +
                    " Please verify your userIds",
            )
        }

        val savedEvents =
            if (events.size > 1) {
                eventRepository.insertRecurringEvent(events)
            } else {
                listOf(eventRepository.insertSingleEvent(events.first()))
            }

        val savedAttendeesByEvent: Map<TeamBalanceId, List<Attendee>> =
            savedEvents
                .map { event -> requestedUsersToAdd.map { userToAdd -> userToAdd.toBeInsertedAttendee(event) } }
                .let { attendees ->
                    attendeeRepository
                        .insertMany(attendees.flatten())
                        .groupBy { it.eventId }
                }

        return savedEvents.map { it.expose(savedAttendeesByEvent[it.teamBalanceId] ?: listOf()) }
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also {
                val firstEvent = it.events.firstOrNull()
                val lastEvent = it.events.lastOrNull()
                log.info(
                    "Created ${it.totalSize} training events " +
                        "with recurringEventId: ${firstEvent?.recurringEventProperties}. " +
                        "First event date: ${firstEvent?.startTime}, " +
                        "Last event date: ${lastEvent?.startTime} ",
                )

                it.events.forEach { e ->
                    log.info(
                        "Created event as part of '${firstEvent?.recurringEventProperties}': $e",
                    )
                }
            }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{training-id}/attendees")
    fun addAttendee(
        @PathVariable(value = "training-id") trainingId: String,
        @RequestParam(value = "all", required = false, defaultValue = "false") addAll: Boolean,
        @RequestBody user: UserAddRequest,
    ): List<AttendeeResponse> {
        val trainingTeamBalanceId = TeamBalanceId(trainingId)
        log.info("Adding: $user (or all: $addAll) to training $trainingTeamBalanceId")
        val training =
            eventRepository.findByIdOrNull(trainingTeamBalanceId) ?: throw InvalidTrainingException(
                trainingTeamBalanceId,
            )
        val users =
            if (addAll) {
                userRepository.findAll()
            } else {
                val userTeamBalanceId = TeamBalanceId(user.userId)
                userRepository
                    .findByIdOrNull(userTeamBalanceId)
                    ?.let(::listOf)
                    ?: throw InvalidUserException(userTeamBalanceId)
            }

        return users.map { u ->
            attendeeRepository.insert(training, u).expose()
        }
    }

    @PutMapping("/{training-id}")
    fun updateTraining(
        @PathVariable(value = "training-id") trainingId: String,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
        @RequestBody updateTrainingRequest: UpdateTrainingRequest,
    ): EventsResponse<TrainingResponse> {
        val trainingTeamBalanceId = TeamBalanceId(trainingId)
        log.info("Updating training $trainingTeamBalanceId with $updateTrainingRequest")
        return trainingService.updateTraining(trainingTeamBalanceId, affectedRecurringEvents, updateTrainingRequest)
            .expose(attendees = emptyList())
            .let { EventsResponse(it.size.toLong(), 1, 1, it.size, it) }
            .also { log.info("Updated training $trainingTeamBalanceId") }
    }

    @PutMapping("/{training-id}/trainer")
    fun updateTrainer(
        @PathVariable(value = "training-id") trainingId: String,
        @RequestBody updateTrainerRequest: UpdateTrainerRequest,
    ): TrainingResponse {
        val trainingTeamBalanceId = TeamBalanceId(trainingId)
        return eventRepository
            .findByIdOrNull(trainingTeamBalanceId)
            ?.let {
                val potentialTrainer = getPotentialTrainer(updateTrainerRequest)
                val updatedTraining =
                    it.copy(
                        trainer = potentialTrainer,
                    )
                eventRepository.updateSingleEvent(updatedTraining)
            }
            ?.expose(emptyList())
            ?.also { log.info("Updated trainer of training $trainingTeamBalanceId. New trainer ${it.trainer}") }
            ?: throw InvalidTrainingException(trainingTeamBalanceId)
    }

    private fun getPotentialTrainer(updateTrainerRequest: UpdateTrainerRequest): User? {
        val userTeamBalanceId: TeamBalanceId? = updateTrainerRequest.userId?.let { TeamBalanceId(it) }
        return when (userTeamBalanceId) {
            null -> null
            else ->
                userRepository
                    .findByIdOrNull(userTeamBalanceId)
                    ?: throw InvalidUserException(userTeamBalanceId)
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{training-id}")
    @Transactional
    fun deleteTraining(
        @PathVariable(value = "training-id") trainingId: String,
        @RequestParam(value = "delete-attendees", defaultValue = "false") deleteAttendees: Boolean,
        @RequestParam(value = "affected-recurring-events") affectedRecurringEvents: AffectedRecurringEvents?,
    ): DeletedEventsResponse {
        val trainingTeamBalanceId = TeamBalanceId(trainingId)
        log.info("Deleting training: $trainingTeamBalanceId")
        if (deleteAttendees) {
            attendeeRepository.findAllAttendeesBelongingToEvent(trainingTeamBalanceId, affectedRecurringEvents)
                .let { attendeeRepository.deleteAll(it) }
        }

        try {
            return eventRepository.deleteById(trainingTeamBalanceId, affectedRecurringEvents)
                .let(::DeletedEventsResponse)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException(
                "Training $trainingTeamBalanceId could not be deleted. There are still attendees bound to this training",
            )
        }
    }
}
