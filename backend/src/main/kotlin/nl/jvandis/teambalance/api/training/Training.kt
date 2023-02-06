package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.NO_ID
import nl.jvandis.teambalance.data.TeamBalanceEntityBuilder
import nl.jvandis.teambalance.data.build
import java.time.LocalDateTime

data class Training(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    val trainer: User? = null,
    val attendees: List<Attendee>? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String? = null, trainer: User? = null) :
        this(
            id = NO_ID,
            startTime = startTime,
            location = location,
            comment = comment,
            trainer = trainer
        )

    fun createUpdatedTraining(updateTrainingRequestBody: UpdateTrainingRequest) = copy(
        startTime = updateTrainingRequestBody.startTime ?: startTime,
        comment = updateTrainingRequestBody.comment ?: comment,
        location = updateTrainingRequestBody.location ?: location

    )

    data class Builder(
        val id: Long,
        val trainerUserId: Long?,
        var event: Event.Builder? = null,
        var trainer: User? = null,
        var attendees: List<Attendee.Builder>? = null
    ) : TeamBalanceEntityBuilder<Training> {
        override fun build(): Training {
            val event = checkNotNull(event) { "Event was not set" }
            check(id == event.id) { "Event id does not match (`id` != event.id)" }

            // TODO: Trainer can be someone that is not an attendee.
            //  This should be changed in the datamodel, as trainer should be part of the attendees!
            // check(trainerUserId == trainer?.id) { "Trainer does does not match the `trainerUserId` property" }

            return Training(
                id = event.id,
                startTime = event.startTime,
                location = event.location,
                comment = event.comment,
                trainer = trainer,
                attendees = attendees?.build()
            )
        }
    }
}
