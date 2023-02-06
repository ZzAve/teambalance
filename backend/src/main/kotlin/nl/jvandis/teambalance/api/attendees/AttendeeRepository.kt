package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.data.valuesFrom
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class AttendeeRepository(
    private val context: DSLContext
) {
    private val log = LoggerFactory.getLogger(AttendeeRepository::class.java)

    fun findAllByEventIdIn(
        eventIds: List<Long>,
        sort: Sort = Sort.by("user.role", "user.name")
    ): List<Attendee> {
        val fetch = context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(ATTENDEE.USER_ID.eq(UZER.ID))
            .where(EVENT.ID.`in`(eventIds))
            .orderBy(UZER.ROLE, UZER.NAME).limit(100)
            .fetchInto(AttendeeWithUserRecordHandler())

        return fetch.build()
    }

    fun insertMany(attendees: List<Attendee>): List<Attendee> {
        if (attendees.isEmpty()) {
            return emptyList()
        }

        val insertResult = context.insertInto(ATTENDEE, ATTENDEE.AVAILABILITY, ATTENDEE.EVENT_ID, ATTENDEE.USER_ID)
            .valuesFrom(attendees, { it.availability.name }, { it.eventId }, { it.user.id })
            .returningResult(ATTENDEE.ID).fetch()

        val attendeesResult =
            context.select()
                .from(ATTENDEE)
                .leftJoin(EVENT)
                .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
                .leftJoin(UZER)
                .on(UZER.ID.eq(ATTENDEE.USER_ID))
                .where(ATTENDEE.ID.`in`(insertResult.mapNotNull(Record1<Long?>::value1)))
                .fetchInto(AttendeeWithUserRecordHandler())

        return if (insertResult.size == attendees.size) {
            attendeesResult.build()
        } else {
            throw DataAccessException("Could not insert jooqAttendees $attendees")
        }
    }

    fun insert(attendee: Attendee): Attendee {
        val attendeeRecord = context
            .insertInto(ATTENDEE, ATTENDEE.AVAILABILITY, ATTENDEE.EVENT_ID, ATTENDEE.USER_ID)
            .values(attendee.availability.name, attendee.eventId, attendee.user.id)
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

    fun findAll(): List<Attendee> =
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .fetchInto(AttendeeWithUserRecordHandler()).build()

    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>): List<Attendee> =
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.EVENT_ID.`in`(eventIds))
            .and(ATTENDEE.USER_ID.`in`(userIds)).fetchInto(AttendeeWithUserRecordHandler()).build()

    fun findAllByUserIdIn(userIds: List<Long>): List<Attendee> =
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.USER_ID.`in`(userIds))
            .fetchInto(AttendeeWithUserRecordHandler()).build()

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
                    .apply { next(it) }
                    .build()
                    .first()
            }

    fun findByUserIdAndEventId(userId: Long, eventId: Long): List<Attendee> =
        context.select()
            .from(ATTENDEE)
            .leftJoin(EVENT)
            .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
            .leftJoin(UZER)
            .on(UZER.ID.eq(ATTENDEE.USER_ID))
            .where(ATTENDEE.USER_ID.eq(userId))
            .and(ATTENDEE.EVENT_ID.eq(eventId))
            .fetchInto(AttendeeWithUserRecordHandler()).build()

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

    fun updateAvailability(attendeeId: Long, availability: Availability): Boolean = context
        .update(ATTENDEE)
        .set(
            ATTENDEE.AVAILABILITY,
            availability.name
        )
        .where(ATTENDEE.ID.eq(attendeeId))
        .execute() == 1
}
