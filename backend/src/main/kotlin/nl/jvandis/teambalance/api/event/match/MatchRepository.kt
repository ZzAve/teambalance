package nl.jvandis.teambalance.api.event.match

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesId
import nl.jvandis.teambalance.api.event.TeamEventTableAndRecordHandler
import nl.jvandis.teambalance.api.event.TeamEventsRepository
import nl.jvandis.teambalance.api.event.deleteStaleRecurringEvent
import nl.jvandis.teambalance.api.event.findAllWithStartTimeAfterImpl
import nl.jvandis.teambalance.api.event.handleWith
import nl.jvandis.teambalance.api.event.insertRecurringEventPropertyRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MATCH
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.loggerFor
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.DatePart
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.localDateTimeAdd
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Repository
class MatchRepository(context: DSLContext) : TeamEventsRepository<Match>(context) {
    override val log = loggerFor()

    override fun findAll(): List<Match> =
        findAllWithStartTimeAfter(LocalDateTime.now().minusYears(5), Pageable.unpaged()).content

    private fun findAllByIds(eventIds: Collection<Long>): List<Match> {
        val recordHandler = MatchWithAttendeesRecordHandler()
        return context
            .select()
            .from(MATCH)
            .leftJoin(EVENT)
            .on(MATCH.ID.eq(EVENT.ID))
            .leftJoin(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .leftJoin(ATTENDEE)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(EVENT.ID.`in`(eventIds))
            .orderBy(
                EVENT.START_TIME,
                UZER.ROLE,
                UZER.NAME,
                EVENT.ID.desc()
            )
            .fetch()
            .handleWith(recordHandler)
    }

    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean
    ): Page<Match> =
        findAllWithStartTimeAfterImpl(
            context,
            since,
            pageable,
            TeamEventTableAndRecordHandler(MATCH, MATCH.ID) { MatchWithAttendeesRecordHandler() }
        )

    override fun findByIdOrNull(eventId: Long): Match? {
        val recordHandler = MatchWithAttendeesRecordHandler()
        return context
            .select()
            .from(MATCH)
            .leftJoin(EVENT)
            .on(MATCH.ID.eq(EVENT.ID))
            .leftJoin(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .where(MATCH.ID.eq(eventId))
            .fetchOne()
            .handleWith(recordHandler)
            .also {
                log.debug(recordHandler.stats())
            }
    }

    @Transactional
    override fun deleteById(eventId: Long, affectedRecurringEvents: AffectedRecurringEvents?): Int {
        val allEventsToDeleteConditions: Condition = allEventsToDeleteCondition(eventId, affectedRecurringEvents)

        val deletedMatchRecords = context.deleteFrom(MATCH)
            .using(EVENT)
            .where(allEventsToDeleteConditions)
            .and(EVENT.ID.eq(MATCH.ID))
            .execute()

        val deletedEventRecords = context.delete(EVENT)
            .where(allEventsToDeleteConditions)
            .execute()

        if (deletedMatchRecords != deletedEventRecords) {
            throw DataAccessException(
                "Tried to delete a different amount of matches ($deletedMatchRecords) " +
                    "from events ($deletedEventRecords)."
            )
        }

        // if recurring event properties are not linked to event anymore, remove recurring event
        if (affectedRecurringEvents != null) {
            deleteStaleRecurringEvent(context)
        }

        return deletedMatchRecords
    }

    override fun insertSingleEvent(event: Match): Match {
        require(event.recurringEventProperties == null) {
            "recurringEventProperties is expected to be null when inserting a single event"
        }

        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .values(event.comment, event.location, event.startTime, null)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .fetchOne()
            ?: throw DataAccessException("Could not insert Match")

        return context
            .insertInto(MATCH, MATCH.COACH, MATCH.HOME_AWAY, MATCH.OPPONENT, MATCH.ID)
            .values(event.coach, event.homeAway, event.opponent, eventRecord[EVENT.ID])
            .returningResult(MATCH.COACH, MATCH.HOME_AWAY, MATCH.OPPONENT, MATCH.ID)
            .fetchOne()
            ?.let { matchRecord ->
                Match(
                    id = matchRecord.getFieldOrThrow(MATCH.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    opponent = matchRecord.getFieldOrThrow(MATCH.OPPONENT),
                    homeAway = matchRecord.getFieldOrThrow(MATCH.HOME_AWAY),
                    coach = matchRecord.getField(MATCH.COACH),
                    recurringEventProperties = null
                )
            }
            ?: throw DataAccessException("Could not insert Match")
    }

    override fun insertRecurringEvent(events: List<Match>): List<Match> {
        if (events.isEmpty()) {
            return emptyList()
        }
        val e = events.first()
        require(
            events.all { it.recurringEventProperties == e.recurringEventProperties }
        ) {
            "All recurringEventProperties are expected to be the same when creating a recurring event"
        }
        val recurringEventProperties = context.insertRecurringEventPropertyRecord(e)

        val insertEventRecordResult = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .valuesFrom(events, { it.comment }, { it.location }, { it.startTime }, { recurringEventProperties.id })
            .returningResult(EVENT.fields().toList())
            .fetch()
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Matches $events. One or more failed") }

        return context
            .insertInto(MATCH, MATCH.COACH, MATCH.HOME_AWAY, MATCH.OPPONENT, MATCH.ID)
            .valuesFrom(
                events,
                { it.coach },
                { it.homeAway },
                { it.opponent },
                { insertEventRecordResult.first { a -> a.getFieldOrThrow(EVENT.START_TIME) == it.startTime }[EVENT.ID] }
            )
            .returningResult(MATCH.COACH, MATCH.HOME_AWAY, MATCH.OPPONENT, MATCH.ID)
            .fetch()
            .map { matchRecord ->
                val eventRecord = insertEventRecordResult.first { a ->
                    a.getFieldOrThrow(EVENT.ID) == matchRecord.getFieldOrThrow(MATCH.ID)
                }
                Match(
                    id = matchRecord.getFieldOrThrow(MATCH.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    opponent = matchRecord.getFieldOrThrow(MATCH.OPPONENT),
                    homeAway = matchRecord.getFieldOrThrow(MATCH.HOME_AWAY),
                    coach = matchRecord.getField(MATCH.COACH),
                    recurringEventProperties = recurringEventProperties
                )
            }
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Matches $events. One or more failed") }
    }

    @Transactional
    override fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        examplarUpdatedEvent: Match,
        durationToAddToEachEvent: Duration
    ): List<Match> {
        val updatedEventIds = context.update(EVENT)
            .set(
                EVENT.START_TIME,
                localDateTimeAdd(EVENT.START_TIME, durationToAddToEachEvent.toSeconds(), DatePart.SECOND)
            )
            .set(EVENT.COMMENT, examplarUpdatedEvent.comment)
            .set(EVENT.LOCATION, examplarUpdatedEvent.location)
            .from(RECURRING_EVENT_PROPERTIES)
            .where(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .and(RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID.eq(recurringEventId.value))
            .returningResult(EVENT.ID)
            .fetch()
            .map { t -> t.getFieldOrThrow(MATCH.ID) }
            .toSet()

        val matchingMatches = context.select(MATCH.ID)
            .from(MATCH)
            .join(EVENT).on(MATCH.ID.eq(EVENT.ID))
            .join(RECURRING_EVENT_PROPERTIES).on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .where(RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID.eq(recurringEventId.value)).asTable("MatchingMatches")

        val updatedMatchesIds = context.update(MATCH)
            .set(MATCH.COACH, examplarUpdatedEvent.coach)
            .set(MATCH.OPPONENT, examplarUpdatedEvent.opponent)
            .set(MATCH.HOME_AWAY, examplarUpdatedEvent.homeAway)
            .from(matchingMatches)
            .where(MATCH.ID.eq(matchingMatches.field(MATCH.ID)))
            .returningResult(MATCH.ID)
            .fetch()
            .map { t -> t.getFieldOrThrow(MATCH.ID) }
            .toSet()

        if (updatedEventIds != updatedMatchesIds) {
            throw DataAccessException("Deleted an different amount of event records ($updatedEventIds) from match records ($updatedMatchesIds)")
        }

        return findAllByIds(updatedEventIds)
    }

    override fun updateSingleEvent(event: Match, removeRecurringEvent: Boolean): Match {
        context
            .update(EVENT)
            .set(EVENT.COMMENT, event.comment)
            .set(EVENT.LOCATION, event.location)
            .set(EVENT.START_TIME, event.startTime)
            .apply {
                if (removeRecurringEvent) {
                    setNull(EVENT.RECURRING_EVENT_ID)
                }
            }
            .where(EVENT.ID.eq(event.id))
            .execute()
            .let { if (it != 1) throw DataAccessException("Could not update Match. EventRecord was not updated") }

        context
            .update(MATCH)
            .set(MATCH.COACH, event.coach)
            .set(MATCH.OPPONENT, event.opponent)
            .set(MATCH.HOME_AWAY, event.homeAway)
            .where(MATCH.ID.eq(event.id))
            .execute()
            .let { if (it != 1) throw DataAccessException("Could not update Match. MatchRecord was not updated") }

        return event
    }
}
