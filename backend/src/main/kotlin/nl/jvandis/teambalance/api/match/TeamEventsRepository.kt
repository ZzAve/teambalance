package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
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
import java.time.LocalDateTime

interface TeamEventsRepository<T : Event> {
    fun findByIdOrNull(eventId: Long): T?
    fun findAll(): List<T>
    fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable,
        withAttendees: Boolean = true
    ): Page<T>

    fun deleteById(eventId: Long): Boolean
    fun update(event: T): T
    fun insert(event: T): T
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

    return context
        .select()
        .from(table)
        .leftJoin(EVENT)
        .on(idField.eq(EVENT.ID))
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
        .fetch().into(handlerFactory())
        .build()
}

// Fixme: naming, docs?
data class TeamEventTableAndRecordHandler<OUT>(
    val table: Table<out Record>,
    val idField: Field<Long?>,
    val handlerFactory: () -> TeamBalanceRecordHandler<OUT>
)
