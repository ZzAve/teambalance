package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.jooq.support.getField
import nl.jvandis.teambalance.api.attendees.AttendeeWithUserRecordHandler
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.RecurringEventProperties
import nl.jvandis.teambalance.api.event.TeamBalanceRecordHandler
import nl.jvandis.teambalance.data.jooq.schema.tables.records.EventRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.MiscellaneousEventRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.RecurringEventPropertiesRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MISCELLANEOUS_EVENT
import org.jooq.Record

class MiscEventWithAttendeesRecordHandler : TeamBalanceRecordHandler<MiscellaneousEvent> {
    private val attendeeRecordHandler = AttendeeWithUserRecordHandler()
    private var recordsHandled = 0L

    private val events = mutableMapOf<Long, Event.Builder>()
    private val recurringEventsPropertiesMap = mutableMapOf<Long, RecurringEventProperties>()
    private val miscEvent = mutableMapOf<Long, MiscellaneousEvent.Builder>()

    private var result: List<MiscellaneousEvent>? = null

    override fun accept(record: Record) {
        recordsHandled++
        val miscEventId = record.getField(MISCELLANEOUS_EVENT.ID)
        val eventId = record.getField(EVENT.ID)
        val recurringEventId = record.getField(EVENT.RECURRING_EVENT_ID)
        if (miscEventId == null || eventId == null) {
            return
        }

        // handle attendee
        attendeeRecordHandler.accept(record)

        val event =
            events.computeIfAbsent(eventId) {
                // mapping via EventRecord works better with column name clashes (like `id`)
                record.into(EventRecord::class.java)
                    .into(Event.Builder::class.java)
            }
        val recurringEventProperties =
            recurringEventId?.let {
                recurringEventsPropertiesMap.computeIfAbsent(it) {
                    record.into(RecurringEventPropertiesRecord::class.java)
                        .into(RecurringEventProperties::class.java)
                }
            }

        val miscEvent =
            miscEvent.computeIfAbsent(miscEventId) {
                // mapping via MiscellaneousEventRecord works better with column name clashes (like `id`)
                record.into(MiscellaneousEventRecord::class.java) //
                    .into(MiscellaneousEvent.Builder::class.java)
            }

        event.recurringEventProperties = recurringEventProperties
        miscEvent.event = event
    }

    fun stats(): String {
        return """
            Nr of records handled: $recordsHandled. 
            Nr of events: ${events.size}. 
            Nr of subEvents: ${miscEvent.size}. 
            -- Attendees:
            ${attendeeRecordHandler.stats()}
            """.trimIndent()
    }

    override fun build(): List<MiscellaneousEvent> =
        result ?: run {
            val jooqAttendees = attendeeRecordHandler.getAttendees()
            val buildResult =
                miscEvent.values.map { builder ->
                    val eventAttendees = jooqAttendees.filter { it.eventId == builder.id }
                    builder.attendees = eventAttendees
                    builder.build()
                }
            result = buildResult
            buildResult
        }
}
