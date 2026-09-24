package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.MultiTenantDslContext
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT_GUEST
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class EventGuestRepository(
    private val context: MultiTenantDslContext,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findAllByEventId(eventId: TeamBalanceId): List<EventGuest> =
        context
            .select()
            .from(EVENT_GUEST)
            .join(EVENT)
            .on(EVENT_GUEST.EVENT_ID.eq(EVENT.ID))
            .where(EVENT.TEAM_BALANCE_ID.eq(eventId.value))
            .fetch()
            .map { record ->
                EventGuest(
                    id = checkNotNull(record.get(EVENT_GUEST.ID)),
                    teamBalanceId = TeamBalanceId(checkNotNull(record.get(EVENT_GUEST.TEAM_BALANCE_ID))),
                    eventId = TeamBalanceId(checkNotNull(record.get(EVENT.TEAM_BALANCE_ID))),
                    name = checkNotNull(record.get(EVENT_GUEST.NAME)),
                    phone = record.get(EVENT_GUEST.PHONE),
                    note = record.get(EVENT_GUEST.NOTE),
                )
            }

    fun insert(
        eventInternalId: Long,
        eventTeamBalanceId: TeamBalanceId,
        name: String,
        phone: String? = null,
        note: String? = null,
    ): EventGuest {
        val newTeamBalanceId = TeamBalanceId.random()
        val insertedId =
            context
                .insertInto(
                    EVENT_GUEST,
                    EVENT_GUEST.TEAM_BALANCE_ID,
                    EVENT_GUEST.EVENT_ID,
                    EVENT_GUEST.NAME,
                    EVENT_GUEST.PHONE,
                    EVENT_GUEST.NOTE,
                ).values(
                    newTeamBalanceId.value,
                    eventInternalId,
                    name,
                    phone,
                    note,
                ).returningResult(EVENT_GUEST.ID)
                .fetchOne()
                ?.value1()
                ?: throw DataAccessException("Could not insert EventGuest for event $eventTeamBalanceId")

        log.debug("Inserted EventGuest with id $insertedId for event $eventTeamBalanceId")

        return EventGuest(
            id = insertedId,
            teamBalanceId = newTeamBalanceId,
            eventId = eventTeamBalanceId,
            name = name,
            phone = phone,
            note = note,
        )
    }

    fun deleteById(guestTeamBalanceId: TeamBalanceId) {
        val deleted =
            context
                .delete(EVENT_GUEST)
                .where(EVENT_GUEST.TEAM_BALANCE_ID.eq(guestTeamBalanceId.value))
                .execute()
        if (deleted != 1) {
            throw DataAccessException("Removed $deleted event guests, expected to remove exactly 1 (id=$guestTeamBalanceId)")
        }
    }
}
