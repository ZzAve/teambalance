package nl.jvandis.teambalance.api.event.training

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.InvalidTrainingException
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.ALL
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT_AND_FUTURE
import nl.jvandis.teambalance.log
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TrainingService(
    private val trainingRepository: TrainingRepository,
) {
    fun updateTraining(
        trainingId: TeamBalanceId,
        affectedRecurringEvents: AffectedRecurringEvents?,
        updateTrainingRequest: UpdateTrainingRequest,
    ): List<Training> {
        val originalTraining =
            trainingRepository.findByIdOrNull(trainingId) ?: throw InvalidTrainingException(
                trainingId,
            )
        require(
            originalTraining.recurringEventProperties?.teamBalanceId?.value ==
                updateTrainingRequest.recurringEventProperties?.id,
        ) {
            "A single training can update only a single training, and a recurring event only a recurring one. " +
                "Current training is ${originalTraining.recurringEventProperties?.teamBalanceId ?: "single"}"
        }
        return if (updateTrainingRequest.recurringEventProperties == null) {
            val updatedTraining = originalTraining.createUpdatedTraining(updateTrainingRequest)
            trainingRepository.updateSingleEvent(updatedTraining).let(::listOf)
        } else {
            require(affectedRecurringEvents != null) {
                "affectedRecurringEvents is expected to be set when updating a recurring event"
            }
            updateRecurringTraining(originalTraining, affectedRecurringEvents, updateTrainingRequest)
        }
    }

    private fun updateRecurringTraining(
        originalTraining: Training,
        affectedRecurringEvents: AffectedRecurringEvents,
        updateTrainingRequest: UpdateTrainingRequest,
    ): List<Training> {
        require(updateTrainingRequest.recurringEventProperties != null && originalTraining.recurringEventProperties != null) {
            """
            RecurringTrainings can only be updated if the \
            `recurringEvent` property is set\
            """
        }
        require(
            updateTrainingRequest.recurringEventProperties.id ==
                originalTraining.recurringEventProperties.teamBalanceId.value,
        ) {
            "Trying to update a recurring event (${updateTrainingRequest.recurringEventProperties.id}) " +
                "through an event that does not belong to that series " +
                "(${originalTraining.recurringEventProperties.teamBalanceId})"
        }

        val teamBalanceId = originalTraining.recurringEventProperties.teamBalanceId

        val updatedTrainings =
            when (affectedRecurringEvents) {
                CURRENT ->
                    trainingRepository
                        .updateSingleEvent(
                            event = originalTraining.createUpdatedTraining(updateTrainingRequest),
                            removeRecurringEvent = true,
                        ).also { log.info("Removed recurringEvent $teamBalanceId from Training with id $originalTraining.id") }
                        .let(::listOf)

                CURRENT_AND_FUTURE ->
                    trainingRepository
                        .partitionRecurringEvent(
                            currentRecurringEventId = teamBalanceId,
                            startTime = originalTraining.startTime,
                            newRecurringEventId = TeamBalanceId.random(),
                        )?.also {
                            log.info(
                                "Split the existing recurringEvent with id $teamBalanceId into 2 separate recurring events. " +
                                    "All events before ${originalTraining.startTime} are part of recurring event " +
                                    "with id $it. The rest is part of recurring event with id $teamBalanceId",
                            )
                        }.run {
                            updateAllFromRecurringEvent(teamBalanceId, originalTraining, updateTrainingRequest)
                        }

                ALL -> updateAllFromRecurringEvent(teamBalanceId, originalTraining, updateTrainingRequest)
            }

        log.info(
            "Updated ${updatedTrainings.size} trainings as part of recurring event " +
                "with id $teamBalanceId: ${updatedTrainings.map { "${it.id} -> ${it.startTime}" }}",
        )
        return updatedTrainings
    }

    private fun updateAllFromRecurringEvent(
        recurringEventId: TeamBalanceId,
        originalTraining: Training,
        updateTrainingRequest: UpdateTrainingRequest,
    ): List<Training> =
        trainingRepository.updateAllFromRecurringEvent(
            recurringEventId = recurringEventId,
            examplarUpdatedEvent = originalTraining.createUpdatedTraining(updateTrainingRequest),
            durationToAddToEachEvent = Duration.between(originalTraining.startTime, updateTrainingRequest.startTime),
        )

    private fun Training.createUpdatedTraining(updateRequest: UpdateTrainingRequest): Training =
        copy(
            startTime = updateRequest.startTime ?: startTime,
            comment = updateRequest.comment ?: comment,
            location = updateRequest.location ?: location,
        )
}
