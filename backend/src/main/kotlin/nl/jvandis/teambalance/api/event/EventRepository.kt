package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.jooq.schema.tables.records.RecurringEventPropertiesRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.count
import org.springframework.stereotype.Repository

@Repository
class EventRepository(
    private val context: DSLContext
) {
    /**
     * Returns whether an event with id {@code eventId} exists
     */
    fun exists(eventId: Long): Boolean =
        context.select(count())
            .from(EVENT)
            .where(EVENT.ID.eq(eventId))
            .fetchOne()
            ?.let { it.value1() == 1 }
            ?: false
}

fun DSLContext.insertRecurringEventPropertyRecord(event: Event): RecurringEventProperties {
    val er = event.recurringEventProperties!!
    val insertRecurringEventPropertiesResult = insertInto(
        RECURRING_EVENT_PROPERTIES,
        RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID,
        RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT,
        RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT,
        RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT,
        RECURRING_EVENT_PROPERTIES.DATE_LIMIT,
        RECURRING_EVENT_PROPERTIES.SELECTED_DAYS
    )
        .values(
            er.teamBalanceId.value,
            er.intervalAmount,
            er.intervalTimeUnit,
            er.amountLimit,
            er.dateLimit,
            er.selectedDays.toTypedArray()
        )
        .returningResult(RECURRING_EVENT_PROPERTIES.fields().toList())
        .fetchOne()
        ?: throw DataAccessException("Couldn't persist RECURRING_EVENT_PROPERTIES for $event")

    return insertRecurringEventPropertiesResult
        .into(RecurringEventPropertiesRecord::class.java)
        .into(RecurringEventProperties::class.java)
}
