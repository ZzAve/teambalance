package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.attendees.AttendeeWithUserRecordHandler
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.match.TeamBalanceRecordHandler
import nl.jvandis.teambalance.data.jooq.schema.tables.records.EventRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.TrainingRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.TRAINING
import org.jooq.Record

class TrainingWithAttendeesRecordHandler : TeamBalanceRecordHandler<Training> {
    private val attendeeRecordHandler = AttendeeWithUserRecordHandler()
    private var recordsHandled = 0L

    private val events = mutableMapOf<Long, Event.Builder>()
    private val trainings = mutableMapOf<Long, Training.Builder>()

    private var result: List<Training>? = null

    override fun accept(record: Record) {
        recordsHandled++
        val trainingId = record[TRAINING.ID]
        val eventId = record[EVENT.ID]
        if (trainingId == null || eventId == null) {
            return
        }

        // handle attendee
        attendeeRecordHandler.accept(record)

        val event = events.computeIfAbsent(eventId) {
            // mapping via EventRecord works better with column name clashes (like `id`)
            record.into(EventRecord::class.java)
                .into(Event.Builder::class.java)
        }

        val training = trainings.computeIfAbsent(trainingId) {
            // mapping via TrainingRecord works better with column name clashes (like `id`)
            record.into(TrainingRecord::class.java) //
                .into(Training.Builder::class.java)
        }

        training.event = event
    }

    fun stats(): String {
        return """
            Nr of records handled: $recordsHandled. 
            Nr of events: ${events.size}. 
            Nr of subEvents: ${trainings.size}. 
            -- Attendees:
            ${attendeeRecordHandler.stats()}
        """.trimIndent()
    }

    override fun build(): List<Training> = result ?: run {
        val jooqAttendees = attendeeRecordHandler.getAttendees()
        val buildResult = trainings.values.map { builder ->
            val eventAttendees = jooqAttendees.filter { it.eventId == builder.id }
            builder.attendees = eventAttendees
            builder.trainer = eventAttendees.firstOrNull { it.user?.id == builder.trainerUserId }?.user
            builder.build()
        }
        result = buildResult
        buildResult
    }
}
