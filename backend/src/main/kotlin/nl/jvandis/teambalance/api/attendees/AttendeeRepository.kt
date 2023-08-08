package nl.jvandis.teambalance.api.attendees

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.jooq.support.valuesFrom
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.ALL
import nl.jvandis.teambalance.api.event.AffectedRecurringEvents.CURRENT_AND_FUTURE
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.Record1
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.noCondition
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class AttendeeRepository(
    private val context: MultiTenantDslContext
) {
    private val log = LoggerFactory.getLogger(AttendeeRepository::class.java)

    fun findAllByEventIdIn(
        eventIds: List<Long>,
        sort: Sort = Sort.by("user.role", "user.name")
    ): List<Attendee> {
        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(ATTENDEE.USER_ID.eq(UZER.ID))
            .where(EVENT.ID.`in`(eventIds))
            .orderBy(UZER.ROLE, UZER.NAME).limit(100)
            .fetch()
            .forEach(recordHandler)

        return recordHandler.build()
    }

    @Transactional
    fun findAllAttendeesBelongingToEvent(
        eventId: Long,
        affectedRecurringEvents: AffectedRecurringEvents?
    ): List<Attendee> {
        var deleteBasedOnCurrentEvent = EVENT.ID.eq(eventId)
        var recurringEventIdCondition = noCondition()
        var recurringEventInFutureCondition = noCondition()
        if (listOf(CURRENT_AND_FUTURE, ALL).contains(affectedRecurringEvents)) {
            val fetchOne = context.select(EVENT.RECURRING_EVENT_ID, EVENT.START_TIME)
                .from(EVENT)
                .where(EVENT.ID.eq(eventId))
                .fetchOne()
            val recurringEventId: Long? = fetchOne?.getField(EVENT.RECURRING_EVENT_ID)
            deleteBasedOnCurrentEvent = noCondition()
            recurringEventIdCondition = EVENT.RECURRING_EVENT_ID.eq(recurringEventId)
            if (affectedRecurringEvents == CURRENT_AND_FUTURE) {
                recurringEventInFutureCondition = EVENT.START_TIME.greaterOrEqual(
                    fetchOne?.getFieldOrThrow(EVENT.START_TIME) ?: LocalDateTime.MIN
                )
            }
        }
        val eventsToDeleteCondition = deleteBasedOnCurrentEvent
            .and(recurringEventIdCondition)
            .and(recurringEventInFutureCondition)

        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .join(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(RECURRING_EVENT_PROPERTIES)
            .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
            .join(UZER)
            .on(ATTENDEE.USER_ID.eq(UZER.ID))
            .where(eventsToDeleteCondition)
            .limit(10000)
            .fetch()
            .forEach(recordHandler)

        return recordHandler.build()
    }

    fun insertMany(attendees: List<Attendee>): List<Attendee> {
        if (attendees.isEmpty()) {
            return emptyList()
        }

        val insertResult = context.insertInto(ATTENDEE, ATTENDEE.AVAILABILITY, ATTENDEE.EVENT_ID, ATTENDEE.USER_ID)
            .valuesFrom(attendees, { it.availability }, { it.eventId }, { it.user.id })
            .returningResult(ATTENDEE.ID).fetch()

        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.ID.`in`(insertResult.mapNotNull(Record1<Long?>::value1)))
            .fetch()
            .forEach(recordHandler)

        return if (insertResult.size == attendees.size) {
            recordHandler.build()
        } else {
            throw DataAccessException("Could not insert jooqAttendees $attendees")
        }
    }

    fun insert(attendee: Attendee): Attendee {
        val attendeeRecord = context
            .insertInto(ATTENDEE, ATTENDEE.AVAILABILITY, ATTENDEE.EVENT_ID, ATTENDEE.USER_ID)
            .values(attendee.availability, attendee.eventId, attendee.user.id)
            .returning(ATTENDEE.ID, ATTENDEE.AVAILABILITY, ATTENDEE.EVENT_ID, ATTENDEE.USER_ID)
            .fetchOne()
            ?: throw DataAccessException("Could not insert Attendee $attendee")

        val user = context.select()
            .from(UZER)
            .where(UZER.ID.eq(attendeeRecord.userId))
            .fetchOne()
            ?.run {
                into(UzerRecord::class.java)
                    .into(User::class.java)
            }
            ?: throw DataAccessException("User with id ${attendeeRecord.userId} doesn't exist. ")

        return attendeeRecord
            .into(Attendee.Builder::class.java)
            .apply { this.user = user }
            .build()
    }

    fun deleteAll(attendees: List<Attendee>): Int = context
        .also { log.info("Deleting attendees: $attendees") }
        .deleteFrom(ATTENDEE)
        .where(ATTENDEE.ID.`in`(attendees.map { it.id }))
        .execute()

    fun findAll(): List<Attendee> {
        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .fetch()
            .forEach(recordHandler)

        return recordHandler.build()
    }

    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>): List<Attendee> {
        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.EVENT_ID.`in`(eventIds))
            .and(ATTENDEE.USER_ID.`in`(userIds))
            .fetch()
            .forEach(recordHandler)

        return recordHandler.build()
    }

    fun findAllByUserIdIn(userIds: List<Long>): List<Attendee> {
        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.USER_ID.`in`(userIds))
            .fetch().forEach(recordHandler)

        return recordHandler.build()
    }

    fun findByIdOrNull(attendeeId: Long): Attendee? =
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.ID.eq(attendeeId))
            .fetchOne()
            ?.let {
                AttendeeWithUserRecordHandler()
                    .apply { accept(it) }
                    .build()
                    .first()
            }

    fun findByUserIdAndEventId(userId: Long, eventId: Long): List<Attendee> {
        val recordHandler = AttendeeWithUserRecordHandler()
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.USER_ID.eq(userId))
            .and(ATTENDEE.EVENT_ID.eq(eventId))
            .fetch().forEach(recordHandler)

        return recordHandler.build()
    }

    fun delete(attendee: Attendee) {
        deleteById(attendee.id)
    }

    fun deleteById(id: Long) {
        if (id == NO_ID) {
            throw IllegalStateException(
                "Attendee with 'special' id $NO_ID can not be deleted. " +
                    "The special no id serves a special purpose in transforming items " +
                    "from records to entities and back"
            )
        }
        val execute = context.delete(ATTENDEE).where(ATTENDEE.ID.eq(id)).execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute attendees, expected to remove only 1")
        }
    }

    fun deleteById(id: Long, affectedRecurringEvents: AffectedRecurringEvents?) {
        if (id == NO_ID) {
            throw IllegalStateException(
                "Attendee with 'special' id $NO_ID can not be deleted. " +
                    "The special no id serves a special purpose in transforming items " +
                    "from records to entities and back"
            )
        }
        val execute = context.delete(ATTENDEE).where(ATTENDEE.ID.eq(id)).execute()
        if (execute != 1) {
            throw DataAccessException("Removed $execute attendees, expected to remove only 1")
        }
    }

    fun updateAvailability(attendeeId: Long, availability: Availability): Boolean = context
        .update(ATTENDEE)
        .set(ATTENDEE.AVAILABILITY, availability)
        .where(ATTENDEE.ID.eq(attendeeId))
        .execute() == 1
}
