package nl.jvandis.teambalance.api.match

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeResponse
import nl.jvandis.teambalance.api.attendees.expose
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.Place
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
data class Match(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    @Column(nullable = false) val opponent: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val homeAway: Place,
    @Column(nullable = true)
    val coach: String? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String?) :
        this(
            0,
            startTime,
            location,
            comment,
            "opponent",
            Place.HOME
        )

    constructor(startTime: LocalDateTime, location: String, comment: String?, opponent: String, homeAway: Place) :
        this(
            id = 0,
            startTime = startTime,
            location = location,
            comment = comment,
            opponent = opponent,
            homeAway = homeAway
        )

    fun createUpdatedMatch(updateMatchRequestBody: UpdateMatchRequest) = copy(
        startTime = updateMatchRequestBody.startTime ?: startTime,
        location = updateMatchRequestBody.location ?: location,
        opponent = updateMatchRequestBody.opponent ?: opponent,
        homeAway = updateMatchRequestBody.homeAway ?: homeAway,
        comment = updateMatchRequestBody.comment ?: comment,
        coach = updateMatchRequestBody.coach ?: coach
    )

    fun externaliseWithAttendees(attendees: List<Attendee>): MatchResponse {
        val attendeesResponse = attendees.map { a -> a.expose() }
        return externalise(attendeesResponse)
    }

    fun externalise(attendeesResponse: List<AttendeeResponse>) = MatchResponse(
        id = id,
        comment = comment,
        location = location,
        startTime = startTime,
        opponent = opponent,
        homeAway = homeAway,
        coach = coach,
        attendees = attendeesResponse
    )
}
