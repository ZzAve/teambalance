package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.InvalidMiscellaneousEventException
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.ALL
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT_AND_FUTURE
import nl.jvandis.teambalance.log
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MiscellaneousEventService(
    private val miscellaneousEventRepository: MiscellaneousEventRepository,
) {
    fun updateMiscellaneousEvent(
        miscellaneousEventId: TeamBalanceId,
        affectedRecurringEvents: AffectedRecurringEvents?,
        updateMiscellaneousEventRequest: UpdateMiscellaneousEventRequest,
    ): List<MiscellaneousEvent> {
        val originalMiscellaneousEvent =
            miscellaneousEventRepository.findByIdOrNull(miscellaneousEventId)
                ?: throw InvalidMiscellaneousEventException(miscellaneousEventId)
        require(
            originalMiscellaneousEvent.recurringEventProperties?.teamBalanceId?.value ==
                updateMiscellaneousEventRequest.recurringEventProperties?.teamBalanceId,
        ) {
            "A single misc event can update only a single misc event, and a recurring event only a recurring one. " +
                "Current event is ${originalMiscellaneousEvent.recurringEventProperties?.teamBalanceId ?: "single"}"
        }
        return if (updateMiscellaneousEventRequest.recurringEventProperties == null) {
            val updatedMiscellaneousEvent =
                originalMiscellaneousEvent.createUpdatedMiscellaneousEvent(updateMiscellaneousEventRequest)
            miscellaneousEventRepository.updateSingleEvent(updatedMiscellaneousEvent).let(::listOf)
        } else {
            require(affectedRecurringEvents != null) {
                "affectedRecurringEvents is expected to be set when updating a recurring event"
            }
            updateRecurringMiscellaneousEvent(
                originalMiscellaneousEvent,
                affectedRecurringEvents,
                updateMiscellaneousEventRequest,
            )
        }
    }

    private fun updateRecurringMiscellaneousEvent(
        originalMiscellaneousEvent: MiscellaneousEvent,
        affectedRecurringEvents: AffectedRecurringEvents,
        updateMiscellaneousEventRequest: UpdateMiscellaneousEventRequest,
    ): List<MiscellaneousEvent> {
        require(
            updateMiscellaneousEventRequest.recurringEventProperties != null && originalMiscellaneousEvent.recurringEventProperties != null,
        ) {
            """
            Recurring MiscellaneousEvents can only be updated if the \
            `recurringEvent` property is set\
            """
        }
        require(
            updateMiscellaneousEventRequest.recurringEventProperties.teamBalanceId ==
                originalMiscellaneousEvent.recurringEventProperties.teamBalanceId.value,
        ) {
            "Trying to update a recurring event (${updateMiscellaneousEventRequest.recurringEventProperties.teamBalanceId}) " +
                "through an event that does not belong to that series " +
                "(${originalMiscellaneousEvent.recurringEventProperties.teamBalanceId})"
        }

        val teamBalanceId = originalMiscellaneousEvent.recurringEventProperties.teamBalanceId

        val updatedMiscellaneousEvents =
            when (affectedRecurringEvents) {
                CURRENT ->
                    miscellaneousEventRepository.updateSingleEvent(
                        event =
                            originalMiscellaneousEvent.createUpdatedMiscellaneousEvent(
                                updateMiscellaneousEventRequest,
                            ),
                        removeRecurringEvent = true,
                    )
                        .also {
                            log.info(
                                "Removed recurringEvent $teamBalanceId from MiscellaneousEvent with id $originalMiscellaneousEvent.id",
                            )
                        }
                        .let(::listOf)

                CURRENT_AND_FUTURE ->
                    miscellaneousEventRepository.partitionRecurringEvent(
                        currentRecurringEventId = teamBalanceId,
                        startTime = originalMiscellaneousEvent.startTime,
                        newRecurringEventId = TeamBalanceId.random(),
                    )
                        ?.also {
                            log.info(
                                "Split the existing recurringEvent with id $teamBalanceId into 2 separate recurring events. " +
                                    "All events before ${originalMiscellaneousEvent.startTime} are part of recurring event " +
                                    "with id $it. The rest is part of recurring event with id $teamBalanceId",
                            )
                        }.run {
                            updateAllFromRecurringEvent(
                                teamBalanceId,
                                originalMiscellaneousEvent,
                                updateMiscellaneousEventRequest,
                            )
                        }

                ALL ->
                    updateAllFromRecurringEvent(
                        teamBalanceId,
                        originalMiscellaneousEvent,
                        updateMiscellaneousEventRequest,
                    )
            }

        log.info(
            "Updated ${updatedMiscellaneousEvents.size} miscellaneousEvents as part of recurring event " +
                "with id $teamBalanceId: ${updatedMiscellaneousEvents.map { "${it.id} -> ${it.startTime}" }}",
        )
        return updatedMiscellaneousEvents
    }

    private fun updateAllFromRecurringEvent(
        recurringEventId: TeamBalanceId,
        originalMiscellaneousEvent: MiscellaneousEvent,
        updateMiscellaneousEventRequest: UpdateMiscellaneousEventRequest,
    ): List<MiscellaneousEvent> {
        return miscellaneousEventRepository.updateAllFromRecurringEvent(
            recurringEventId = recurringEventId,
            examplarUpdatedEvent =
                originalMiscellaneousEvent.createUpdatedMiscellaneousEvent(
                    updateMiscellaneousEventRequest,
                ),
            durationToAddToEachEvent =
                Duration.between(
                    originalMiscellaneousEvent.startTime,
                    updateMiscellaneousEventRequest.startTime,
                ),
        )
    }

    private fun MiscellaneousEvent.createUpdatedMiscellaneousEvent(updateRequest: UpdateMiscellaneousEventRequest) =
        copy(
            startTime = updateRequest.startTime ?: startTime,
            comment = updateRequest.comment ?: comment,
            location = updateRequest.location ?: location,
            title = updateRequest.title ?: title,
        )
}
