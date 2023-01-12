package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.users.User
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Training(
    override val id: Long,
    override val startTime: LocalDateTime,
    override val location: String,
    override val comment: String? = null,
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    val trainer: User? = null
) : Event(id, startTime, location, comment) {
    constructor(startTime: LocalDateTime, location: String, comment: String? = null, trainer: User? = null) :
        this(
            id = 0,
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
}
