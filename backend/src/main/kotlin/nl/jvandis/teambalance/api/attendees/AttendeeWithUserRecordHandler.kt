package nl.jvandis.teambalance.api.attendees

import nl.jvandis.jooq.support.getFieldOrThrow
import nl.jvandis.teambalance.api.event.TeamBalanceRecordHandler
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.toUserBuilder
import nl.jvandis.teambalance.data.jooq.schema.tables.records.AttendeeRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.EVENT
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.Record

class AttendeeWithUserRecordHandler : TeamBalanceRecordHandler<Attendee> {
    private val attendees = mutableMapOf<Long, Attendee.Builder>()
    private val users = mutableMapOf<Long, User.Builder>()

    private var recordsHandled = 0L
    private var result: List<Attendee>? = null

    override fun accept(record: Record) {
        recordsHandled++
        val fields = record.fields().toList()
        val attendeeId =
            if (fields.contains(ATTENDEE.AVAILABILITY)) record.into(AttendeeRecord::class.java).id else null
        val userId =
            if (fields.contains(UZER.ROLE)) record.into(UzerRecord::class.java)?.id else null
        if (attendeeId == null || userId == null) {
            return
        }

        val attendee =
            attendees.computeIfAbsent(attendeeId) {
                // mapping via AttendeeRecords works better with column name clashes (like `id`)
                val attendeeRecord = record.into(AttendeeRecord::class.java)
                Attendee.Builder(
                    id = checkNotNull(attendeeRecord.id),
                    teamBalanceId = checkNotNull(attendeeRecord.teamBalanceId),
                    userId = checkNotNull(attendeeRecord.userId),
                    availability = checkNotNull(attendeeRecord.availability),
                    eventId = record.getFieldOrThrow(EVENT.TEAM_BALANCE_ID),
                )
            }

        val user =
            users.computeIfAbsent(userId) {
                record.into(UzerRecord::class.java).toUserBuilder()
            }

        attendee.user = user
    }

    fun getAttendees(): List<Attendee.Builder> = attendees.values.toList()

    fun stats(): String =
        """
        Nr of records handled: $recordsHandled.
        Nr of attendeesCreated: ${attendees.size}.
        Nr of users created: ${users.size}"
        """.trimIndent()

    override fun build(): List<Attendee> =
        result ?: run {
            val buildResult = attendees.values.map { it.build() }
            result = buildResult
            buildResult
        }
}
