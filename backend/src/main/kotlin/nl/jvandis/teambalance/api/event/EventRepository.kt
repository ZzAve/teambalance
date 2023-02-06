package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import org.jooq.DSLContext
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
