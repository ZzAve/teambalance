package nl.jvandis.teambalance.api.training

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.match.TeamEventTableAndRecordHandler
import nl.jvandis.teambalance.api.match.TeamEventsRepository
import nl.jvandis.teambalance.api.match.findAllWithStartTimeAfterImpl
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class TrainingRepository(
    private val context: DSLContext
) : TeamEventsRepository<Training> {

    private val entity = TeamEventTableAndRecordHandler(TRAINING, TRAINING.ID) { TrainingWithAttendeesRecordHandler() }
    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean
    ): Page<Training> =
        findAllWithStartTimeAfterImpl(context, since, pageable, entity)

    override fun findByIdOrNull(eventId: Long): Training? {
        val possibleTraining = context
            .select()
            .from(TRAINING)
            .leftJoin(EVENT)
            .on(TRAINING.ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(TRAINING.TRAINER_USER_ID.eq(UZER.ID))
            .where(TRAINING.ID.eq(eventId))
            .fetchOne()

        val user = possibleTraining
            ?.takeIf { it[UZER.ID] != null }
            ?.into(UzerRecord::class.java)
            ?.into(User::class.java)

        return possibleTraining
            ?.let { record ->
                Training(
                    id = record.getFieldOrThrow(TRAINING.ID),
                    startTime = record.getFieldOrThrow(EVENT.START_TIME),
                    location = record.getFieldOrThrow(EVENT.LOCATION),
                    comment = record.getField(EVENT.COMMENT),
                    trainer = user
                )
            }
    }

    override fun deleteById(eventId: Long): Boolean {
        val trainingDeleteSuccess = context.delete(TRAINING)
            .where(TRAINING.ID.eq(eventId))
            .execute() == 1

        val eventDeleteSuccess = context.delete(EVENT)
            .where(EVENT.ID.eq(eventId))
            .execute() == 1

        // FIXME: if one of the two fails, you want to rollback... how?
        return trainingDeleteSuccess && eventDeleteSuccess
    }

    override fun insertMany(events: List<Training>): List<Training> {
        if (events.isEmpty()) {
            return emptyList()
        }

        val insertEventRecordResult = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
            .valuesFrom(events, { it.comment }, { it.location }, { it.startTime })
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
            .fetch()
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Trainings $events. One or more failed") }

        return context
            .insertInto(TRAINING, TRAINING.TRAINER_USER_ID, TRAINING.ID)
            .valuesFrom(
                events,
                { it.trainer?.id },
                { insertEventRecordResult.first { a -> a.getFieldOrThrow(EVENT.START_TIME) == it.startTime }[EVENT.ID] })
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
                    trainer = events.first { it.startTime == eventRecord.getFieldOrThrow(EVENT.START_TIME) }.trainer // do better, pretty
                )

            }
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Trainings $events. One or more failed") }

    }


    override fun insert(event: Training): Training {
        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
            .values(event.comment, event.location, event.startTime)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
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
                    trainer = event.trainer // do better, pretty
                )
            }
            ?: throw DataAccessException("Could not insert Match")
    }

    override fun update(event: Training): Training {
        context
            .update(EVENT)
            .set(EVENT.COMMENT, event.comment)
            .set(EVENT.LOCATION, event.location)
            .set(EVENT.START_TIME, event.startTime)
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
