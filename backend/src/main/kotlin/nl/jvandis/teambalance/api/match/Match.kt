package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.Place
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder
import nl.jvandis.teambalance.data.build
import java.time.LocalDateTime

data class Match(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String?,
    val opponent: String,
    val homeAway: Place,
    val coach: String?,
    val attendees: List<Attendee>? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String?) :
            this(
                id = NO_ID,
                startTime = startTime,
                location = location,
                comment = comment,
                opponent = "opponent",
                homeAway = Place.HOME,
                coach = null
            )

    constructor(startTime: LocalDateTime, location: String, comment: String?, opponent: String, homeAway: Place) :
            this(
                id = NO_ID,
                startTime = startTime,
                location = location,
                comment = comment,
                opponent = opponent,
                homeAway = homeAway,
                coach = null
            )

    fun createUpdatedMatch(updateMatchRequestBody: UpdateMatchRequest) = copy(
        startTime = updateMatchRequestBody.startTime ?: startTime,
        location = updateMatchRequestBody.location ?: location,
        opponent = updateMatchRequestBody.opponent ?: opponent,
        homeAway = updateMatchRequestBody.homeAway ?: homeAway,
        comment = updateMatchRequestBody.comment ?: comment, // TODO allow setting `null`?
        coach = updateMatchRequestBody.coach ?: coach // TODO allow setting 'null'?
    )

    data class Builder(
        val id: Long,
        val opponent: String,
        val homeAway: Place,
        val coach: String?,
        var event: Event.Builder? = null,
        var attendees: List<Attendee.Builder>? = null
    ) : TeamBalanceEntityBuilder<Match> {
        override fun build(): Match {
            val event = checkNotNull(event) { "Event was not set" }
            check(id == event.id) { "Event id does not match (`id` != event.id)" }

            return Match(
                id = event.id,
                startTime = event.startTime,
                location = event.location,
                comment = event.comment,
                opponent = opponent,
                homeAway = homeAway,
                coach = coach,
                attendees = attendees?.build()
            )
        }

    }
}
