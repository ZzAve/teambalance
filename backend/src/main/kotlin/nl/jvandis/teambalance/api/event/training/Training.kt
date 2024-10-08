package nl.jvandis.teambalance.api.event.training

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.RecurringEventProperties
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder
import nl.jvandis.teambalance.data.build
import java.time.LocalDateTime

data class Training(
    override val id: Long,
    override val teamBalanceId: TeamBalanceId,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    override val recurringEventProperties: RecurringEventProperties?,
    val trainer: User? = null,
    val attendees: List<Attendee>? = null,
) : Event(id, teamBalanceId, startTime, location, comment, recurringEventProperties) {
    internal constructor(
        startTime: LocalDateTime,
        location: String,
        comment: String? = null,
        trainer: User? = null,
        recurringEventProperties: RecurringEventProperties? = null,
    ) :
        this(
            id = NO_ID,
            teamBalanceId = TeamBalanceId.random(),
            startTime = startTime,
            location = location,
            comment = comment,
            trainer = trainer,
            recurringEventProperties = recurringEventProperties,
        )

    data class Builder(
        val id: Long,
        val trainerUserId: Long?,
        var event: Event.Builder? = null,
        var trainer: User? = null,
        var attendees: List<Attendee.Builder>? = null,
    ) : TeamBalanceEntityBuilder<Training> {
        override fun build(): Training {
            val event = checkNotNull(event) { "Event was not set" }
            check(id == event.id) { "Event id does not match (`id` != event.id)" }

            // TODO: Trainer can be someone that is not an attendee.
            //  This should be changed in the datamodel, as trainer should be part of the attendees!
            // check(trainerUserId == trainer?.id) { "Trainer does does not match the `trainerUserId` property" }
            event.validate()

            return Training(
                id = event.id,
                teamBalanceId = event.teamBalanceId,
                startTime = event.startTime,
                location = event.location,
                comment = event.comment,
                trainer = trainer,
                attendees = attendees?.build(),
                recurringEventProperties = event.recurringEventProperties,
            )
        }
    }
}
