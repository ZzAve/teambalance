package nl.jvandis.teambalance.api.event

import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.jooq.schema.tables.records.RecurringEventPropertiesRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.RECURRING_EVENT_PROPERTIES
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.count
import org.springframework.stereotype.Repository

@Repository
class EventRepository(
    private val context: MultiTenantDslContext,
) {
    /**
     * Returns whether an event with id {@code eventId} exists
     */
    fun exists(eventId: TeamBalanceId): Boolean =
        context.select(count())
            .from(EVENT)
            .where(EVENT.TEAM_BALANCE_ID.eq(eventId.value))
            .fetchOne()
            ?.let { it.value1() == 1 }
            ?: false

    fun findInternalId(eventId: TeamBalanceId): Long? =
        context.select()
            .from(EVENT)
            .where(EVENT.TEAM_BALANCE_ID.eq(eventId.value))
            .fetchOne()
            ?.getFieldOrThrow(EVENT.ID)
}

fun MultiTenantDslContext.insertRecurringEventPropertyRecord(event: Event): RecurringEventProperties {
    val recurringEventProperties =
        checkNotNull(event.recurringEventProperties) { "Expression 'event.recurringEventProperties' must not be null" }
    val insertRecurringEventPropertiesResult =
        insertInto(
            RECURRING_EVENT_PROPERTIES,
            RECURRING_EVENT_PROPERTIES.TEAM_BALANCE_ID,
            RECURRING_EVENT_PROPERTIES.INTERVAL_AMOUNT,
            RECURRING_EVENT_PROPERTIES.INTERVAL_TIME_UNIT,
            RECURRING_EVENT_PROPERTIES.AMOUNT_LIMIT,
            RECURRING_EVENT_PROPERTIES.DATE_LIMIT,
            RECURRING_EVENT_PROPERTIES.SELECTED_DAYS,
        )
            .values(
                recurringEventProperties.teamBalanceId.value,
                recurringEventProperties.intervalAmount,
                recurringEventProperties.intervalTimeUnit,
                recurringEventProperties.amountLimit,
                recurringEventProperties.dateLimit,
                recurringEventProperties.selectedDays.toTypedArray(),
            )
            .returningResult(RECURRING_EVENT_PROPERTIES.fields().toList())
            .fetchOne()
            ?: throw DataAccessException("Couldn't persist RECURRING_EVENT_PROPERTIES for $event")

    return insertRecurringEventPropertiesResult
        .into(RecurringEventPropertiesRecord::class.java)
        .into(RecurringEventProperties::class.java)
}
