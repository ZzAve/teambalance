package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.NO_ID

data class EventGuest(
    val id: Long = NO_ID,
    val teamBalanceId: TeamBalanceId,
    val eventId: TeamBalanceId,
    val name: String,
    val phone: String? = null,
    val note: String? = null,
)
