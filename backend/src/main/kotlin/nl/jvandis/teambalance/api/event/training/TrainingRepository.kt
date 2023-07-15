package nl.jvandis.teambalance.api.event.training

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.ALL
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT_AND_FUTURE
import nl.jvandis.teambalance.api.event.LoggingContext
import nl.jvandis.teambalance.api.event.RecurringEventPropertiesId
import nl.jvandis.teambalance.api.event.TeamEventTableAndRecordHandler
import nl.jvandis.teambalance.api.event.TeamEventsRepository
import nl.jvandis.teambalance.api.event.deleteStaleRecurringEvent
import nl.jvandis.teambalance.api.event.findAllWithStartTimeAfterImpl
import nl.jvandis.teambalance.api.event.handleWith
import nl.jvandis.teambalance.api.event.insertRecurringEventPropertyRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.loggerFor
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.DatePart
import org.jooq.Field
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.localDateTimeAdd
import org.jooq.impl.DSL.select
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Repository
class TrainingRepository(
    private val context: DSLContext
) : TeamEventsRepository<Training>, LoggingContext {
    override val log = loggerFor()

    private fun findAllByIds(eventIds: List<Long>): List<Training> {
        val recordHandler = TrainingWithAttendeesRecordHandler()
        return context
            .select()
            .from(TRAINING)
            .leftJoin(EVENT)
            .on(TRAINING.ID.eq(EVENT.ID))
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
    ): Page<Training> =
        findAllWithStartTimeAfterImpl(
            context,
            since,
            pageable,
            TeamEventTableAndRecordHandler(TRAINING, TRAINING.ID) { TrainingWithAttendeesRecordHandler() }
        )

    override fun findByIdOrNull(eventId: Long): Training? {
        val recordHandler = TrainingWithAttendeesRecordHandler()
        return context
            .select()
            .from(TRAINING)
            .leftJoin(EVENT)
            .on(TRAINING.ID.eq(EVENT.ID))
            .leftJoin(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .leftJoin(UZER)
            .on(TRAINING.TRAINER_USER_ID.eq(UZER.ID))
            .where(TRAINING.ID.eq(eventId))
            .fetchOne()
            .handleWith(recordHandler)
            .also {
                log.debug(recordHandler.stats())
            }
    }

    @Transactional
    override fun deleteById(eventId: Long, affectedRecurringEvents: AffectedRecurringEvents?): Int {
        val eventDetails =
            context.select(EVENT.RECURRING_EVENT_ID, EVENT.START_TIME).from(EVENT).where(EVENT.ID.eq(eventId))
                .asTable("EventDetails")

        val recurringEventId: Field<Long?> = eventDetails.field(EVENT.RECURRING_EVENT_ID)
            ?: throw DataAccessException("RecurringEventId is not set for recurring event")

        val allEventsToDeleteConditions: Condition = when (affectedRecurringEvents) {
            ALL -> EVENT.ID.`in`(
                select(EVENT.ID)
                    .from(EVENT)
                    .join(eventDetails)
                    .on(recurringEventId.eq(EVENT.RECURRING_EVENT_ID))
                    .where(EVENT.RECURRING_EVENT_ID.eq(recurringEventId))
            )

            CURRENT_AND_FUTURE -> EVENT.ID.`in`(
                select(EVENT.ID).from(EVENT)
                    .join(eventDetails)
                    .on(recurringEventId.eq(EVENT.RECURRING_EVENT_ID))
                    .where(EVENT.RECURRING_EVENT_ID.eq(recurringEventId))
                    .and(EVENT.START_TIME.greaterOrEqual(eventDetails.field(EVENT.START_TIME)))
            )

            CURRENT, null -> EVENT.ID.eq(eventId)
        }

        val deletedTrainingRecords = context.deleteFrom(TRAINING)
            .using(EVENT)
            .where(allEventsToDeleteConditions)
            .and(EVENT.ID.eq(TRAINING.ID))
            .execute()

        val deletedEventRecords = context.delete(EVENT)
            .where(allEventsToDeleteConditions)
            .execute()

        if (deletedTrainingRecords != deletedEventRecords) {
            throw DataAccessException(
                "Tried to delete a different amount of trainings ($deletedTrainingRecords) " +
                    "from events ($deletedEventRecords)."
            )
        }

        // if recurring event properties are not linked to event anymore, remove recurring event
        if (affectedRecurringEvents != null) {
            deleteStaleRecurringEvent(context)
        }

        return deletedTrainingRecords
    }

    @Transactional
    override fun partitionRecurringEvent(
        currentRecurringEventId: RecurringEventPropertiesId,
        startTime: LocalDateTime,
        newRecurringEventId: RecurringEventPropertiesId
    ): RecurringEventPropertiesId? {
        // fetch one event with an earlier startTime
        val record = context
            .select()
            .from(EVENT)
            .join(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .where(RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID.eq(currentRecurringEventId.value))
            .and(EVENT.START_TIME.lessThan(startTime))
            .limit(1)
            .fetchOne()

        if (record == null) {
            log.info(
                "There are no events belonging to recurringEvent $currentRecurringEventId " +
                    "with startTime before $startTime. No partition needed"
            )
            return null
        }

        // insert recurring event
        val recurringEventProperties = context.insertInto(
            RECURRING_EVENT_PROPERTIES,
            RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID,
            RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT,
            RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT,
            RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT,
            RECURRING_EVENT_PROPERTIES.DATE_LIMIT,
            RECURRING_EVENT_PROPERTIES.SELECTED_DAYS
        )
            .values(
                newRecurringEventId.value,
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT),
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT),
                record.getField(RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT),
                record.getField(RECURRING_EVENT_PROPERTIES.DATE_LIMIT),
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.SELECTED_DAYS)
            )
            .returningResult(RECURRING_EVENT_PROPERTIES.fields().toList())
            .fetchOne()
            ?: throw DataAccessException("Couldn't persist a new RECURRING_EVENT_PROPERTIES")

        // update all event with earlier timestamp to refer to recurring event
        val updatedEvents = context.update(EVENT)
            .set(EVENT.RECURRING_EVENT_ID, recurringEventProperties.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.ID))
            .where(EVENT.RECURRING_EVENT_ID.eq(record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.ID)))
            .and(EVENT.START_TIME.lessThan(startTime))
            .execute()

        log.info(
            "Moved $updatedEvents events belonging to recurring event $currentRecurringEventId " +
                "from before $startTime to a new recurring event $newRecurringEventId. "
        )
        return newRecurringEventId
    }

    override fun removeRecurringEvent(eventId: Long) {
        context.update(EVENT)
            .setNull(EVENT.RECURRING_EVENT_ID)
            .where(EVENT.ID.eq(eventId))
            .execute()
            .apply {
                if (this != 1) {
                    throw DataAccessException("There was more than 1 event with id $eventId. Data is corrupt!")
                }
            }
    }

    override fun insertRecurringEvent(events: List<Training>): List<Training> {
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
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Trainings $events. One or more failed") }

        return context
            .insertInto(TRAINING, TRAINING.TRAINER_USER_ID, TRAINING.ID)
            .valuesFrom(
                events,
                { it.trainer?.id },
                { insertEventRecordResult.first { a -> a.getFieldOrThrow(EVENT.START_TIME) == it.startTime }[EVENT.ID] }
            )
            .returningResult(TRAINING.ID, TRAINING.TRAINER_USER_ID)
            .fetch()
            .map { trainingRecord ->
                val eventRecord = insertEventRecordResult.first { a ->
                    a.getFieldOrThrow(EVENT.ID) == trainingRecord.getFieldOrThrow(TRAINING.ID)
                }
                Training(
                    id = trainingRecord.getFieldOrThrow(TRAINING.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    trainer = events.first { it.startTime == eventRecord.getFieldOrThrow(EVENT.START_TIME) }.trainer, // do better, pretty
                    recurringEventProperties = recurringEventProperties
                )
            }
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Trainings $events. One or more failed") }
    }

    @Transactional
    override fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        examplarUpdatedTraining: Training,
        durationToAddToEachEvent: Duration
    ): List<Training> {
        val updatedEventIds = context.update(
            EVENT
        )
            .set(
                EVENT.START_TIME,
                localDateTimeAdd(EVENT.START_TIME, durationToAddToEachEvent.toSeconds(), DatePart.SECOND)
            )
            .set(EVENT.COMMENT, examplarUpdatedTraining.comment)
            .set(EVENT.LOCATION, examplarUpdatedTraining.location)
            .from(RECURRING_EVENT_PROPERTIES)
            .where(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .and(RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID.eq(recurringEventId.value))
            .returningResult(EVENT.ID)
            .fetch()

        return findAllByIds(updatedEventIds.map { it.getFieldOrThrow(EVENT.ID) })
    }

    override fun insertSingleEvent(event: Training): Training {
        require(event.recurringEventProperties == null) {
            "recurringEventProperties is expected to be null when inserting a single event"
        }

        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .values(event.comment, event.location, event.startTime, null)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME, EVENT.RECURRING_EVENT_ID)
            .fetchOne()
            ?: throw DataAccessException("Could not insert Training")

        return context
            .insertInto(TRAINING, TRAINING.TRAINER_USER_ID, TRAINING.ID)
            .values(event.trainer?.id, eventRecord[EVENT.ID])
            .returningResult(TRAINING.ID, TRAINING.TRAINER_USER_ID)
            .fetchOne()
            ?.let { trainingRecord ->
                Training(
                    id = trainingRecord.getFieldOrThrow(TRAINING.ID),
                    startTime = eventRecord.getFieldOrThrow(EVENT.START_TIME),
                    location = eventRecord.getFieldOrThrow(EVENT.LOCATION),
                    comment = eventRecord.getField(EVENT.COMMENT),
                    trainer = event.trainer, // do better, pretty
                    recurringEventProperties = null
                )
            }
            ?: throw DataAccessException("Could not insert Match")
    }

    override fun updateSingleEvent(event: Training, removeRecurringEvent: Boolean): Training {
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
            .let { if (it != 1) throw DataAccessException("Could not update Training. EventRecord not updated") }

        context
            .update(TRAINING)
            .set(TRAINING.TRAINER_USER_ID, event.trainer?.id)
            .where(TRAINING.ID.eq(event.id))
            .execute()
            .let { if (it != 1) throw DataAccessException("Could not update Training. TrainingRecord not updated") }

        // okay? or fetch from db the result?
        return event
    }

    override fun findAll(): List<Training> {
        return findAllWithStartTimeAfter(LocalDateTime.now().minusYears(5), Pageable.unpaged()).content
    }
}
