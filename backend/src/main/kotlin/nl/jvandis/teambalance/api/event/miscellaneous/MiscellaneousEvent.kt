package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder
import nl.jvandis.teambalance.data.build
import java.time.LocalDateTime

data class MiscellaneousEvent(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    val title: String? = null,
    val attendees: List<Attendee>? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String?, title: String?) :
        this(
            id = NO_ID,
            startTime = startTime,
            location = location,
            comment = comment,
            title = title
        )

    fun createUpdatedEvent(updateEventRequest: UpdateMiscellaneousEventRequest) = copy(
        startTime = updateEventRequest.startTime ?: startTime,
        comment = updateEventRequest.comment ?: comment,
        location = updateEventRequest.location ?: location,
        title = updateEventRequest.title ?: title
    )

    data class Builder (
        val id: Long,
        val title: String?,
        var event: Event.Builder?,
        var attendees: List<Attendee.Builder>?
    ): TeamBalanceEntityBuilder<MiscellaneousEvent>{
        override fun build(): MiscellaneousEvent {
            val event = checkNotNull(event) { "Event was not set" }
            check(id == event.id) { "Event id does not match (`id` != event.id)" }

            return MiscellaneousEvent(
                id = event.id,
                startTime = event.startTime,
                location = event.location,
                comment = event.comment,
                title = title,
                attendees = attendees?.build()
            )

        }

    }
}
