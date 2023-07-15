package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.data.limitOrDefault
import nl.jvandis.teambalance.data.offsetOrDefault
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Record1
import org.jooq.Table
import org.jooq.impl.DSL.count
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Duration
import java.time.LocalDateTime

interface TeamEventsRepository<T : Event> {
    fun findByIdOrNull(eventId: Long): T?
    fun findAll(): List<T>
    fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean = true
    ): Page<T>

    fun deleteById(eventId: Long, affectedRecurringEvents: AffectedRecurringEvents? = null): Int
    fun updateSingleEvent(event: T, removeRecurringEvent: Boolean = false): T
    fun insertSingleEvent(event: T): T
    fun insertRecurringEvent(events: List<T>): List<T>

    /**
     * Partitions the recurringEvent with the given `recurringEventId`, into:
     *
     * - a recurringEvent with a startTime before the given startTime, and with a new Id
     * - a recurringEvent with events with a startTime at or after the given startTime.
     *
     * The latter recurringEvent will keep the provided id, the former will get a new one, which is also returned.
     *
     * @return the recurringEventId which is applied to all events before the given startTime. If no events occur
     * before the given startTime, it returns null
     */
    fun partitionRecurringEvent(
        currentRecurringEventId: RecurringEventPropertiesId,
        startTime: LocalDateTime,
        newRecurringEventId: RecurringEventPropertiesId
    ): RecurringEventPropertiesId?

    /**
     * Removes the recurringEventId from the event with the provided `id`
     */
    fun removeRecurringEvent(eventId: Long): Unit

    /**
     * Update all events in a recurring event series.It is up to the implementer to choose the fields to update.
     * No guarantees are given about which ones are, other than the startTime, which is updated according to
     * the provided `durationToAddToEachEvent`
     */
    fun updateAllFromRecurringEvent(
        recurringEventId: RecurringEventPropertiesId,
        examplarUpdatedTraining: T,
        durationToAddToEachEvent: Duration
    ): List<T>
}

inline fun <reified EVENT : Event> findAllWithStartTimeAfterImpl(
    context: DSLContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EVENT>
): Page<EVENT> {
    val totalCount = eventsCount(context, since, pageable, entity)
    val events = eventsOfType(context, since, pageable, entity)

    return PageImpl(events, pageable, totalCount.toLong()) // TODO: Move away from Spring Data Page?
}

fun <EVENT : Event> eventsCount(
    context: DSLContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EVENT>
): Int {
    val (table: Table<out Record>, idField: Field<Long?>) = entity
    return context
        .select(count())
        .from(table)
        .leftJoin(EVENT)
        .on(idField.eq(EVENT.ID))
        .where(EVENT.START_TIME.greaterOrEqual(since))
        .fetchOne { t: Record1<Int> -> t.value1() }
        ?: throw IllegalStateException("Could not perform 'count' on ${EVENT::class.simpleName} table, request params: {since:$since, pageable:$pageable}")
}

fun <EV : Event> eventsOfType(
    context: DSLContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EV>
): List<EV> {
    val (table: Table<out Record>, idField: Field<Long?>, handlerFactory: () -> TeamBalanceRecordHandler<EV>) = entity
    val startTimeSort = EVENT.START_TIME.let {
        if (pageable.sort.getOrderFor("startTime")?.isDescending == true) {
            it.desc()
        } else {
            it.asc()
        }
    }

    val eligibleEventIds = context
        .select(EVENT.ID)
        .from(table)
        .leftJoin(EVENT)
        .on(idField.eq(EVENT.ID))
        .where(EVENT.START_TIME.greaterOrEqual(since))
        .orderBy(startTimeSort, EVENT.ID.desc())
        .offset(offsetOrDefault(pageable))
        .limit(limitOrDefault(pageable))

    val recordHandler = handlerFactory()
    return context
        .select()
        .from(table)
        .leftJoin(EVENT)
        .on(idField.eq(EVENT.ID))
        .leftJoin(RECURRING_EVENT_PROPERTIES)
        .on(EVENT.RECURRING_EVENT_ID.eq(RECURRING_EVENT_PROPERTIES.ID))
        .leftJoin(ATTENDEE)
        .on(ATTENDEE.EVENT_ID.eq(EVENT.ID))
        .leftJoin(UZER)
        .on(UZER.ID.eq(ATTENDEE.USER_ID))
        .where(EVENT.ID.`in`(eligibleEventIds))
        .orderBy(
            startTimeSort,
            UZER.ROLE,
            UZER.NAME,
            EVENT.ID.desc()
        )
        .fetch()
        .handleWith(recordHandler)
}

/**
 * if recurring event properties are not linked to event anymore, remove recurring event
 */
context(LoggingContext)
fun deleteStaleRecurringEvent(context: DSLContext) {
    log.info("Trying to delete stale recurringEventProperties records")
    // CTE: which recurring event properties are not linked from any event
    val staleRecurringEventProperties =
        context.select(RECURRING_EVENT_PROPERTIES.ID).from(RECURRING_EVENT_PROPERTIES).leftAntiJoin(EVENT).on(
            EVENT.RECURRING_EVENT_ID.eq(
                RECURRING_EVENT_PROPERTIES.ID
            )
        ).asTable("StaleRecurringEventProperties")

    context.delete(RECURRING_EVENT_PROPERTIES)
        .using(staleRecurringEventProperties)
        .where(
            RECURRING_EVENT_PROPERTIES.ID.eq(staleRecurringEventProperties.field(RECURRING_EVENT_PROPERTIES.ID))
        )
        .returning()
        .fetch()
        .also {
            if (it.size > 1) {
                log.warn(
                    "More than 1 recurringEvent was deleted when trying stale recurring event properties."
                )
            }
        }
}

// Fixme: naming, docs?
data class TeamEventTableAndRecordHandler<OUT>(
    val table: Table<out Record>,
    val idField: Field<Long?>,
    val handlerFactory: () -> TeamBalanceRecordHandler<OUT>
)
