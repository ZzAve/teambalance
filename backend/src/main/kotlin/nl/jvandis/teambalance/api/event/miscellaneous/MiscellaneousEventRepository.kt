package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.match.TeamEventTableAndRecordHandler
import nl.jvandis.teambalance.api.match.TeamEventsRepository
import nl.jvandis.teambalance.api.match.findAllWithStartTimeAfterImpl
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MISCELLANEOUS_EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MiscellaneousEventRepository(
    private val context: DSLContext
) : TeamEventsRepository<MiscellaneousEvent> {

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

    override fun findByIdOrNull(eventId: Long): MiscellaneousEvent? = context
        .select()
        .from(MISCELLANEOUS_EVENT)
        .leftJoin(EVENT)
        .on(MISCELLANEOUS_EVENT.ID.eq(EVENT.ID))
        .where(MISCELLANEOUS_EVENT.ID.eq(eventId))
        .fetchOne()
        ?.into(MiscellaneousEvent::class.java)

    override fun deleteById(eventId: Long): Boolean {
        val miscEventDeleteSuccess = context.delete(MISCELLANEOUS_EVENT)
            .where(MISCELLANEOUS_EVENT.ID.eq(eventId))
            .execute() == 1

        val eventDeleteSuccess = context.delete(EVENT)
            .where(EVENT.ID.eq(eventId))
            .execute() == 1

        return miscEventDeleteSuccess && eventDeleteSuccess
    }

    override fun insert(event: MiscellaneousEvent): MiscellaneousEvent {
        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
            .values(event.comment, event.location, event.startTime)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
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
                    title = matchRecord.getField(MISCELLANEOUS_EVENT.TITLE)
                )
            }
            ?: throw DataAccessException("Could not insert MiscEvent")
    }

    override fun insertMany(events: List<MiscellaneousEvent>): List<MiscellaneousEvent> {
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
            .insertInto(MISCELLANEOUS_EVENT, MISCELLANEOUS_EVENT.TITLE, MISCELLANEOUS_EVENT.ID)
            .valuesFrom(events, { it.title },
                { insertEventRecordResult.first { a -> a.getFieldOrThrow(EVENT.START_TIME) == it.startTime }[EVENT.ID] })
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
                    title = matchRecord.getField(MISCELLANEOUS_EVENT.TITLE)
                )
            }
            .also { if (it.size != events.size) throw DataAccessException("Could not insert Trainings $events. One or more failed") }

    }

    override fun update(event: MiscellaneousEvent): MiscellaneousEvent {
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
