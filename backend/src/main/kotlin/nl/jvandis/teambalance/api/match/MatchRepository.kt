package nl.jvandis.teambalance.api.match

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MATCH
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MatchRepository(
    private val context: DSLContext
) : TeamEventsRepository<Match> {

    private val entity = TeamEventTableAndRecordHandler(MATCH, MATCH.ID) { MatchWithAttendeesRecordHandler() }

    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean
    ): Page<Match> =
        findAllWithStartTimeAfterImpl(context, since, pageable, entity)

    override fun findAll(): List<Match> =
        findAllWithStartTimeAfter(LocalDateTime.now().minusYears(5), Pageable.unpaged()).content

    override fun insert(event: Match): Match {
        val eventRecord = context
            .insertInto(EVENT, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
            .values(event.comment, event.location, event.startTime)
            .returningResult(EVENT.ID, EVENT.COMMENT, EVENT.LOCATION, EVENT.START_TIME)
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
                    coach = matchRecord.getField(MATCH.COACH)

                )
            }
            ?: throw DataAccessException("Could not insert Match")
    }

    override fun update(event: Match): Match {
        context
            .update(EVENT)
            .set(EVENT.COMMENT, event.comment)
            .set(EVENT.LOCATION, event.location)
            .set(EVENT.START_TIME, event.startTime)
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

    override fun findByIdOrNull(eventId: Long): Match? = context
        .select()
        .from(MATCH)
        .leftJoin(EVENT)
        .on(MATCH.ID.eq(EVENT.ID))
        .where(MATCH.ID.eq(eventId))
        .fetchOne()
        ?.into(Match::class.java)

    override fun deleteById(eventId: Long): Boolean {
        val matchDeleteSuccess = context.delete(MATCH)
            .where(MATCH.ID.eq(eventId))
            .execute() == 1

        val eventDeleteSuccess = context.delete(EVENT)
            .where(EVENT.ID.eq(eventId))
            .execute() == 1

        return matchDeleteSuccess && eventDeleteSuccess
    }
}
