package nl.jvandis.teambalance.api.event.match

import nl.jvandis.teambalance.api.InvalidMatchException
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.ALL
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT_AND_FUTURE
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesId
import nl.jvandis.teambalance.loggerFor
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MatchService(
    private val matchRepository: MatchRepository
) {
    private val log = loggerFor()

    fun updateMatch(
        matchId: Long,
        affectedRecurringEvents: AffectedRecurringEvents?,
        updateMatchRequest: UpdateMatchRequest
    ): List<Match> {
        val originalMatch =
            matchRepository.findByIdOrNull(matchId) ?: throw InvalidMatchException(matchId)
        require(originalMatch.recurringEventProperties?.teamBalanceId?.value == updateMatchRequest.recurringEventProperties?.teamBalanceId) {
            "A single match can update only a single match, and a recurring event only a recurring one. " +
                "Current match is ${originalMatch.recurringEventProperties?.teamBalanceId ?: "single"}"
        }
        return if (updateMatchRequest.recurringEventProperties == null) {
            val updatedMatch = originalMatch.createUpdatedMatch(updateMatchRequest)
            matchRepository.updateSingleEvent(updatedMatch).let(::listOf)
        } else {
            require(affectedRecurringEvents != null) {
                "affectedRecurringEvents is expected to be set when updating a recurring event"
            }
            updateRecurringMatch(originalMatch, affectedRecurringEvents, updateMatchRequest)
        }
    }

    private fun updateRecurringMatch(
        originalMatch: Match,
        affectedRecurringEvents: AffectedRecurringEvents,
        updateMatchRequest: UpdateMatchRequest
    ): List<Match> {
        require(updateMatchRequest.recurringEventProperties != null && originalMatch.recurringEventProperties != null) {
            "RecurringMatchs can only be updated if the " +
                "`recurringEvent` property is set"
        }
        require(
            updateMatchRequest.recurringEventProperties.teamBalanceId ==
                originalMatch.recurringEventProperties.teamBalanceId.value
        ) {
            "Trying to update a recurring event (${updateMatchRequest.recurringEventProperties.teamBalanceId}) " +
                "through an event that does not belong to that series " +
                "(${originalMatch.recurringEventProperties.teamBalanceId})"
        }

        val teamBalanceId = originalMatch.recurringEventProperties.teamBalanceId

        val updatedMatchs = when (affectedRecurringEvents) {
            CURRENT -> matchRepository.updateSingleEvent(
                event = originalMatch.createUpdatedMatch(updateMatchRequest),
                removeRecurringEvent = true
            )
                .also { log.info("Removed recurringEvent $teamBalanceId from Match with id $originalMatch.id") }
                .let(::listOf)

            CURRENT_AND_FUTURE -> matchRepository.partitionRecurringEvent(
                currentRecurringEventId = teamBalanceId,
                startTime = originalMatch.startTime,
                newRecurringEventId = RecurringEventPropertiesId.create()
            )
                ?.also {
                    log.info(
                        "Split the existing recurringEvent with id $teamBalanceId into 2 separate recurring events. " +
                            "All events before ${originalMatch.startTime} are part of recurring event " +
                            "with id $it. The rest is part of recurring event with id $teamBalanceId"
                    )
                }.run {
                    updateAllFromRecurringEvent(teamBalanceId, originalMatch, updateMatchRequest)
                }

            ALL -> updateAllFromRecurringEvent(teamBalanceId, originalMatch, updateMatchRequest)
        }

        log.info(
            "Updated ${updatedMatchs.size} matchs as part of recurring event " +
                "with id $teamBalanceId: ${updatedMatchs.map { "${it.id} -> ${it.startTime}" }}"
        )
        return updatedMatchs
    }

    private fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        originalMatch: Match,
        updateMatchRequest: UpdateMatchRequest
    ): List<Match> {
        return matchRepository.updateAllFromRecurringEvent(
            recurringEventId = recurringEventId,
            examplarUpdatedEvent = originalMatch.createUpdatedMatch(updateMatchRequest),
            durationToAddToEachEvent = Duration.between(originalMatch.startTime, updateMatchRequest.startTime)
        )
    }

    fun Match.createUpdatedMatch(updateMatchRequestBody: UpdateMatchRequest) = copy(
        startTime = updateMatchRequestBody.startTime ?: startTime,
        location = updateMatchRequestBody.location ?: location,
        opponent = updateMatchRequestBody.opponent ?: opponent,
        homeAway = updateMatchRequestBody.homeAway ?: homeAway,
        comment = updateMatchRequestBody.comment ?: comment
    )
}
