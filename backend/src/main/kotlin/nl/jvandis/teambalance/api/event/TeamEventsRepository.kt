package nl.jvandis.teambalance.api.event

import nl.jvandis.jooq.support.getField
import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import nl.jvandis.teambalance.data.limitOrDefault
import nl.jvandis.teambalance.data.offsetOrDefault
import nl.jvandis.teambalance.log
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record
import org.jooq.Record1
import org.jooq.Table
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.jooq.impl.DSL.count
import org.slf4j.Logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

abstract class TeamEventsRepository<T : Event>(
    protected val context: MultiTenantDslContext,
) : LoggingContext {
    override fun log(): Logger = log

    abstract fun findByIdOrNull(eventId: TeamBalanceId): T?

    abstract fun findAll(): List<T>

    abstract fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean = true,
    ): Page<T>

    abstract fun deleteById(
        eventId: TeamBalanceId,
        affectedRecurringEvents: AffectedRecurringEvents? = null,
    ): Int

    abstract fun updateSingleEvent(
        event: T,
        removeRecurringEvent: Boolean = false,
    ): T

    abstract fun insertSingleEvent(event: T): T

    abstract fun insertRecurringEvent(events: List<T>): List<T>

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
    @Transactional
    open fun partitionRecurringEvent(
        currentRecurringEventId: TeamBalanceId,
        startTime: LocalDateTime,
        newRecurringEventId: TeamBalanceId,
    ): TeamBalanceId? = partitionRecurringEvent(context, currentRecurringEventId, startTime, newRecurringEventId, log())

    /**
     * Removes the recurringEventId from the event with the provided `id`
     */
    fun removeRecurringEvent(eventId: Long) =
        context
            .update(EVENT)
            .setNull(EVENT.RECURRING_EVENT_ID)
            .where(EVENT.ID.eq(eventId))
            .execute()
            .apply {
                if (this != 1) {
                    throw DataAccessException("There was more than 1 event with id $eventId. Data is corrupt!")
                }
            }

    /**
     * Update all events in a recurring event series.It is up to the implementer to choose the fields to update.
     * No guarantees are given about which ones are updated other than the startTime, which is updated according to
     * the provided {@code durationToAddToEachEvent}
     */
    abstract fun updateAllFromRecurringEvent(
        recurringEventId: TeamBalanceId,
        examplarUpdatedEvent: T,
        durationToAddToEachEvent: Duration,
    ): List<T>

    protected fun allEventsToDeleteCondition(
        eventId: TeamBalanceId,
        affectedRecurringEvents: AffectedRecurringEvents?,
    ): Condition {
        val eventDetails =
            context
                .select(EVENT.RECURRING_EVENT_ID, EVENT.START_TIME)
                .from(EVENT)
                .where(EVENT.TEAM_BALANCE_ID.eq(eventId.value))
                .asTable("EventDetails")

        val recurringEventId: Field<Long?> =
            eventDetails.field(EVENT.RECURRING_EVENT_ID)
                ?: throw DataAccessException("RecurringEventId is not set for recurring event")

        val allEventsToDeleteConditions: Condition =
            when (affectedRecurringEvents) {
                AffectedRecurringEvents.ALL ->
                    EVENT.ID.`in`(
                        DSL
                            .select(EVENT.ID)
                            .from(EVENT)
                            .join(eventDetails)
                            .on(recurringEventId.eq(EVENT.RECURRING_EVENT_ID))
                            .where(EVENT.RECURRING_EVENT_ID.eq(recurringEventId)),
                    )

                AffectedRecurringEvents.CURRENT_AND_FUTURE ->
                    EVENT.ID.`in`(
                        DSL
                            .select(EVENT.ID)
                            .from(EVENT)
                            .join(eventDetails)
                            .on(recurringEventId.eq(EVENT.RECURRING_EVENT_ID))
                            .where(EVENT.RECURRING_EVENT_ID.eq(recurringEventId))
                            .and(EVENT.START_TIME.greaterOrEqual(eventDetails.field(EVENT.START_TIME))),
                    )

                AffectedRecurringEvents.CURRENT, null -> EVENT.TEAM_BALANCE_ID.eq(eventId.value)
            }
        return allEventsToDeleteConditions
    }
}

inline fun <reified EVENT : Event> findAllWithStartTimeAfterImpl(
    context: MultiTenantDslContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EVENT>,
): Page<EVENT> {
    val totalCount = eventsCount(context, since, pageable, entity)
    val events = eventsOfType(context, since, pageable, entity)

    return PageImpl(events, pageable, totalCount.toLong()) // TODO: Move away from Spring Data Page?
}

fun <EVENT : Event> eventsCount(
    context: MultiTenantDslContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EVENT>,
): Int {
    val (table: Table<out Record>, idField: Field<Long?>) = entity
    return context
        .select(count())
        .from(table)
        .leftJoin(EVENT)
        .on(idField.eq(EVENT.ID))
        .where(EVENT.START_TIME.greaterOrEqual(since))
        .fetchOne { t: Record1<Int> -> t.value1() }
        ?: throw IllegalStateException(
            "Could not perform 'count' on ${EVENT::class.simpleName} table, " +
                "request params: {since:$since, pageable:$pageable}",
        )
}

fun <EV : Event> eventsOfType(
    context: MultiTenantDslContext,
    since: LocalDateTime,
    pageable: Pageable,
    entity: TeamEventTableAndRecordHandler<EV>,
): List<EV> {
    val (table: Table<out Record>, idField: Field<Long?>, handlerFactory: () -> TeamBalanceRecordHandler<EV>) = entity
    val startTimeSort =
        EVENT.START_TIME.let {
            if (pageable.sort.getOrderFor("startTime")?.isDescending == true) {
                it.desc()
            } else {
                it.asc()
            }
        }

    val eligibleEventIds =
        context
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
            EVENT.ID.desc(),
        ).fetch()
        .handleWith(recordHandler)
}

/**
 * if recurring event properties are not linked to event anymore, remove recurring event
 */
fun deleteStaleRecurringEvent(
    context: MultiTenantDslContext,
    log: Logger,
) {
    log.info("Trying to delete stale recurringEventProperties records")
    // CTE: which recurring event properties are not linked from any event
    val staleRecurringEventProperties =
        context
            .select(RECURRING_EVENT_PROPERTIES.ID)
            .from(RECURRING_EVENT_PROPERTIES)
            .leftAntiJoin(EVENT)
            .on(
                EVENT.RECURRING_EVENT_ID.eq(
                    RECURRING_EVENT_PROPERTIES.ID,
                ),
            ).asTable("StaleRecurringEventProperties")

    context
        .delete(RECURRING_EVENT_PROPERTIES)
        .using(staleRecurringEventProperties)
        .where(
            RECURRING_EVENT_PROPERTIES.ID.eq(staleRecurringEventProperties.field(RECURRING_EVENT_PROPERTIES.ID)),
        ).returning()
        .fetch()
        .also {
            if (it.size > 1) {
                log.warn(
                    "More than 1 recurringEvent was deleted when trying stale recurring event properties.",
                )
            }
        }
}

private fun partitionRecurringEvent(
    context: MultiTenantDslContext,
    currentRecurringEventId: TeamBalanceId,
    startTime: LocalDateTime,
    newRecurringEventId: TeamBalanceId,
    log: Logger,
): TeamBalanceId? {
    // fetch one event with an earlier startTime
    val record =
        context
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
                "with startTime before $startTime. No partition needed",
        )
        return null
    }

    // insert recurring event
    val recurringEventProperties =
        context
            .insertInto(
                RECURRING_EVENT_PROPERTIES,
                RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID,
                RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT,
                RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT,
                RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT,
                RECURRING_EVENT_PROPERTIES.DATE_LIMIT,
                RECURRING_EVENT_PROPERTIES.SELECTED_DAYS,
            ).values(
                newRecurringEventId.value,
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT),
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT),
                record.getField(RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT),
                record.getField(RECURRING_EVENT_PROPERTIES.DATE_LIMIT),
                record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.SELECTED_DAYS),
            ).returningResult(RECURRING_EVENT_PROPERTIES.fields().toList())
            .fetchOne()
            ?: throw DataAccessException("Couldn't persist a new RECURRING_EVENT_PROPERTIES")

    // update all event with earlier timestamp to refer to recurring event
    val updatedEvents =
        context
            .update(EVENT)
            .set(EVENT.RECURRING_EVENT_ID, recurringEventProperties.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.ID))
            .where(EVENT.RECURRING_EVENT_ID.eq(record.getFieldOrThrow(RECURRING_EVENT_PROPERTIES.ID)))
            .and(EVENT.START_TIME.lessThan(startTime))
            .execute()

    log.info(
        "Moved $updatedEvents events belonging to recurring event $currentRecurringEventId " +
            "from before $startTime to a new recurring event $newRecurringEventId. ",
    )
    return newRecurringEventId
}

// Fixme: naming, docs?
data class TeamEventTableAndRecordHandler<OUT>(
    val table: Table<out Record>,
    val idField: Field<Long?>,
    val handlerFactory: () -> TeamBalanceRecordHandler<OUT>,
)
