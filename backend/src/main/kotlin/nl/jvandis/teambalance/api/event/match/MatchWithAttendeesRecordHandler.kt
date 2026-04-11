package nl.jvandis.teambalance.api.event.match

import nl.jvandis.jooq.support.getField
import nl.jvandis.teambalance.api.attendees.AttendeeWithUserRecordHandler
import nl.jvandis.teambalance.api.event.Event
import nl.jvandis.teambalance.api.event.RecurringEventProperties
import nl.jvandis.teambalance.api.event.TeamBalanceRecordHandler
import nl.jvandis.teambalance.api.event.toRecurringEventProperties
import nl.jvandis.teambalance.data.jooq.schema.tables.records.EventRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.MatchRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.RecurringEventPropertiesRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.MATCH
import org.jooq.Record

class MatchWithAttendeesRecordHandler : TeamBalanceRecordHandler<Match> {
    private val attendeeRecordHandler = AttendeeWithUserRecordHandler()
    private var recordsHandled = 0L

    private val events = mutableMapOf<Long, Event.Builder>()
    private val recurringEventsPropertiesMap = mutableMapOf<Long, RecurringEventProperties>()
    private val matches = mutableMapOf<Long, Match.Builder>()

    private var result: List<Match>? = null

    override fun accept(record: Record) {
        recordsHandled++
        val matchId = record.getField(MATCH.ID)
        val eventId = record.getField(EVENT.ID)
        val recurringEventId = record.getField(EVENT.RECURRING_EVENT_ID)
        if (matchId == null || eventId == null) {
            return
        }

        // handle attendee
        attendeeRecordHandler.accept(record)

        val event =
            events.computeIfAbsent(eventId) {
                // mapping via EventRecord works better with column name clashes (like `id`)
                record.into(EventRecord::class.java).let { er ->
                    Event.Builder(
                        id = checkNotNull(er.id),
                        teamBalanceId = checkNotNull(er.teamBalanceId),
                        startTime = checkNotNull(er.startTime),
                        location = checkNotNull(er.location),
                        comment = er.comment,
                        recurringEventId = er.recurringEventId,
                        recurringEventProperties = null,
                    )
                }
            }
        val recurringEventProperties =
            recurringEventId?.let {
                recurringEventsPropertiesMap.computeIfAbsent(it) {
                    record
                        .into(RecurringEventPropertiesRecord::class.java)
                        .toRecurringEventProperties()
                }
            }

        val match =
            matches.computeIfAbsent(matchId) {
                // mapping via MatchRecord works better with column name clashes (like `id`)
                record.into(MatchRecord::class.java).let { mr ->
                    Match.Builder(
                        id = checkNotNull(mr.id),
                        opponent = checkNotNull(mr.opponent),
                        homeAway = checkNotNull(mr.homeAway),
                        additionalInfo = mr.additionalInfo,
                    )
                }
            }

        event.recurringEventProperties = recurringEventProperties
        match.event = event
    }

    fun stats(): String =
        """
        Nr of records handled: $recordsHandled.
        Nr of events: ${events.size}.
        Nr of subEvents: ${matches.size}.
        -- Attendees:
        ${attendeeRecordHandler.stats()}
        """.trimIndent()

    override fun build(): List<Match> =
        result ?: run {
            val jooqAttendees = attendeeRecordHandler.getAttendees()
            val buildResult =
                matches.values.map { builder ->
                    val event = checkNotNull(builder.event) { "Event was not set" }
                    val eventAttendees = jooqAttendees.filter { it.eventId == event.teamBalanceId }
                    builder.attendees = eventAttendees
                    builder.build()
                }
            result = buildResult
            buildResult
        }
}
