package nl.jvandis.teambalance.api.event.miscellaneous

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
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MISCELLANEOUS_EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.loggerFor
import org.jooq.Condition
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
class MiscellaneousEventRepository(context: MultiTenantDslContext) : TeamEventsRepository<MiscellaneousEvent>(context) {
    override val log = loggerFor()

    override fun findAll(): List<MiscellaneousEvent> =
        findAllWithStartTimeAfter(LocalDateTime.now().minusYears(5), Pageable.unpaged()).content

    private fun findAllByIds(eventIds: Collection<Long>): List<MiscellaneousEvent> {
        val recordHandler = MiscEventWithAttendeesRecordHandler()
        return context
            .select()
            .from(MISCELLANEOUS_EVENT)
            .leftJoin(EVENT)
            .on(MISCELLANEOUS_EVENT.ID.eq(EVENT.ID))
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
    ): Page<MiscellaneousEvent> =
        findAllWithStartTimeAfterImpl(
            context,
            since,
            pageable,
            TeamEventTableAndRecordHandler(
                MISCELLANEOUS_EVENT,
                MISCELLANEOUS_EVENT.ID
            ) { MiscEventWithAttendeesRecordHandler() }
        )

    override fun findByIdOrNull(eventId: Long): MiscellaneousEvent? {
        val recordHandler = MiscEventWithAttendeesRecordHandler()
        return context
            .select()
            .from(MISCELLANEOUS_EVENT)
            .leftJoin(EVENT)
            .on(MISCELLANEOUS_EVENT.ID.eq(EVENT.ID))
            .leftJoin(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .where(MISCELLANEOUS_EVENT.ID.eq(eventId))
            .fetchOne()
            .handleWith(recordHandler)
            .also {
                log.debug(recordHandler.stats())
            }
    }

    @Transactional
    override fun deleteById(eventId: Long, affectedRecurringEvents: AffectedRecurringEvents?): Int {
        val allEventsToDeleteConditions: Condition = allEventsToDeleteCondition(eventId, affectedRecurringEvents)

        val deletedMiscEventRecords = context.delete(MISCELLANEOUS_EVENT)
            .using(EVENT)
            .where(allEventsToDeleteConditions)
            .and(EVENT.ID.eq(MISCELLANEOUS_EVENT.ID))
            .execute()

        val deletedEventRecords = context.delete(EVENT)
            .where(allEventsToDeleteConditions)
            .execute()

        if (deletedMiscEventRecords != deletedEventRecords) {
            throw DataAccessException(
                "Tried to delete a different amount of events ($deletedMiscEventRecords) " +
                    "from events ($deletedEventRecords)."
            )
        }

        // if recurring event properties are not linked to event anymore, remove recurring event
        if (affectedRecurringEvents != null) {
            deleteStaleRecurringEvent(context)
        }

        return deletedMiscEventRecords
    }

    override fun insertSingleEvent(event: MiscellaneousEvent): MiscellaneousEvent {
        require(event.recurringEventProperties == null) {
            "recurringEventProperties is expected to be null when inserting a single event"
        }

        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .values(event.comment, event.location, event.startTime, null)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .fetchOne()
            ?: throw DataAccessException("Could not insert Event part of MiscEvent")

        return context
            .insertInto(MISCELLANEOUS_EVENT, MISCELLANEOUS_EVENT.TITLE, MISCELLANEOUS_EVENT.ID)
            .values(event.title, eventRecord[EVENT.ID])
            .returningResult(MISCELLANEOUS_EVENT.TITLE, MISCELLANEOUS_EVENT.ID)
            .fetchOne()
            ?.let { matchRecord ->
                MiscellaneousEvent(
                    id = matchRecord.getFieldOrThrow(MISCELLANEOUS_EVENT.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    title = matchRecord.getField(MISCELLANEOUS_EVENT.TITLE),
                    recurringEventProperties = null
                )
            }
            ?: throw DataAccessException("Could not insert MiscEvent")
    }

    override fun insertRecurringEvent(events: List<MiscellaneousEvent>): List<MiscellaneousEvent> {
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
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Events $events. One or more failed") }

        return context
            .insertInto(MISCELLANEOUS_EVENT, MISCELLANEOUS_EVENT.TITLE, MISCELLANEOUS_EVENT.ID)
            .valuesFrom(
                events,
                { it.title },
                { insertEventRecordResult.first { a -> a.getFieldOrThrow(EVENT.START_TIME) == it.startTime }[EVENT.ID] }
            )
            .returningResult(MISCELLANEOUS_EVENT.TITLE, MISCELLANEOUS_EVENT.ID)
            .fetch()
            .map { matchRecord ->
                val eventRecord = insertEventRecordResult.first { a ->
                    a.getFieldOrThrow(EVENT.ID) == matchRecord.getFieldOrThrow(TRAINING.ID)
                }
                MiscellaneousEvent(
                    id = matchRecord.getFieldOrThrow(MISCELLANEOUS_EVENT.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    title = matchRecord.getField(MISCELLANEOUS_EVENT.TITLE),
                    recurringEventProperties = recurringEventProperties
                )
            }
            .also { if (it.size != events.size) throw DataAccessException("Could not insert MiscEvents $events. One or more failed") }
    }

    @Transactional
    override fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        examplarUpdatedEvent: MiscellaneousEvent,
        durationToAddToEachEvent: Duration
    ): List<MiscellaneousEvent> {
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
            .map { t -> t.getFieldOrThrow(EVENT.ID) }
            .toSet()

        val matchingMiscEvents = context.select(MISCELLANEOUS_EVENT.ID)
            .from(MISCELLANEOUS_EVENT)
            .join(EVENT).on(MISCELLANEOUS_EVENT.ID.eq(EVENT.ID))
            .join(RECURRING_EVENT_PROPERTIES).on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .where(RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID.eq(recurringEventId.value)).asTable("MatchingMiscEvents")

        val updatedMiscEventIds = context.update(MISCELLANEOUS_EVENT)
            .set(MISCELLANEOUS_EVENT.TITLE, examplarUpdatedEvent.title)
            .from(matchingMiscEvents)
            .where(MISCELLANEOUS_EVENT.ID.eq(matchingMiscEvents.field(MISCELLANEOUS_EVENT.ID)))
            .returningResult(MISCELLANEOUS_EVENT.ID)
            .fetch()
            .map { t -> t.getFieldOrThrow(MISCELLANEOUS_EVENT.ID) }
            .toSet()

        if (updatedEventIds != updatedMiscEventIds) {
            throw DataAccessException("Deleted an different amount of event records ($updatedEventIds) from match records ($updatedMiscEventIds)")
        }

        return findAllByIds(updatedEventIds)
    }

    override fun updateSingleEvent(event: MiscellaneousEvent, removeRecurringEvent: Boolean): MiscellaneousEvent {
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
            .let { if (it != 1) throw DataAccessException("Could not update MiscEvent. EventRecord was not updated") }

        context
            .update(MISCELLANEOUS_EVENT)
            .set(MISCELLANEOUS_EVENT.TITLE, event.title)
            .where(MISCELLANEOUS_EVENT.ID.eq(event.id))
            .execute()
            .let { if (it != 1) throw DataAccessException("Could not update MiscEvent. MiscEventRecord was not updated") }

        return event
    }
}
