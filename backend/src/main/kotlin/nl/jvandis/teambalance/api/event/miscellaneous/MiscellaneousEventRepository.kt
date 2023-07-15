package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesId
import nl.jvandis.teambalance.api.event.TeamEventTableAndRecordHandler
import nl.jvandis.teambalance.api.event.TeamEventsRepository
import nl.jvandis.teambalance.api.event.findAllWithStartTimeAfterImpl
import nl.jvandis.teambalance.api.event.handleWith
import nl.jvandis.teambalance.api.event.insertRecurringEventPropertyRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MISCELLANEOUS_EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import nl.jvandis.teambalance.loggerFor
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime

@Repository
class MiscellaneousEventRepository(
    private val context: DSLContext
) : TeamEventsRepository<MiscellaneousEvent> {
    private val LOG = loggerFor()

    private val entity = TeamEventTableAndRecordHandler(
        MISCELLANEOUS_EVENT,
        MISCELLANEOUS_EVENT.ID
    ) { MiscEventWithAttendeesRecordHandler() }

    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean
    ): Page<MiscellaneousEvent> =
        findAllWithStartTimeAfterImpl(context, since, pageable, entity)

    override fun findAll(): List<MiscellaneousEvent> =
        findAllWithStartTimeAfter(LocalDateTime.now().minusYears(5), Pageable.unpaged()).content

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
                LOG.debug(recordHandler.stats())
            }
    }

    override fun deleteById(eventId: Long, affectedRecurringEvents: AffectedRecurringEvents?): Int {
        val deletedMiscEventRecords = context.delete(MISCELLANEOUS_EVENT)
            .where(MISCELLANEOUS_EVENT.ID.eq(eventId))
            .execute()

        val recurringEventPropertiesDeleteSuccess = context.delete(RECURRING_EVENT_PROPERTIES)
            .using(
                EVENT
                    .join(RECURRING_EVENT_PROPERTIES)
                    .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            )
            .where(EVENT.ID.eq(eventId))
            .execute()

        val eventDeleteSuccess = context.delete(EVENT)
            .where(EVENT.ID.eq(eventId))
            .execute()

        // TODO
        return deletedMiscEventRecords
    }

    override fun partitionRecurringEvent(
        currentRecurringEventId: RecurringEventPropertiesId,
        startTime: LocalDateTime,
        newRecurringEventId: RecurringEventPropertiesId
    ): RecurringEventPropertiesId {
        TODO("Not yet implemented")
    }

    override fun removeRecurringEvent(eventId: Long) {
        TODO("Not yet implemented")
    }

    override fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        examplarUpdatedTraining: MiscellaneousEvent,
        durationToAddToEachEvent: Duration
    ): List<MiscellaneousEvent> {
        TODO("Not yet implemented")
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
            e.recurringEventProperties != null
        ) {
            "RecurringEventProperties should be set for a recurring event..."
        }
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

    override fun updateSingleEvent(event: MiscellaneousEvent, removeRecurringEvent: Boolean): MiscellaneousEvent {
        context
            .update(EVENT)
            .set(EVENT.COMMENT, event.comment)
            .set(EVENT.LOCATION, event.location)
            .set(EVENT.START_TIME, event.startTime)
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
