package nl.jvandis.teambalance.api.attendees

import nl.jvandis.teambalance.api.match.TeamBalanceRecordHandler
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.data.jooq.schema.tables.records.AttendeeRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.records.UzerRecord
import nl.jvandis.teambalance.data.jooq.schema.tables.references.ATTENDEE
import nl.jvandis.teambalance.data.jooq.schema.tables.references.UZER
import org.jooq.Record

class AttendeeWithUserRecordHandler : TeamBalanceRecordHandler<Attendee> {
    private val attendees = mutableMapOf<Long, Attendee.Builder>()
    private val users = mutableMapOf<Long, User>()

    private var recordsHandled = 0L
    private var result: List<Attendee>? = null

    override fun next(r: Record) {
        recordsHandled++
        val attendeeId = r[ATTENDEE.ID]
        val userId = r[UZER.ID]
        if (attendeeId == null || userId == null) {
            return
        }

        val attendee = attendees.computeIfAbsent(attendeeId) {
            // mapping via AttendeeRecords works better with column name clashes (like `id`)
            r.into(AttendeeRecord::class.java)
                .into(Attendee.Builder::class.java)
        }

        val user = users.computeIfAbsent(userId) {
            r.into(UzerRecord::class.java)
                .into(User::class.java)
        }

        attendee.user = user
    }

    fun getAttendees(): List<Attendee.Builder> {
        return attendees.values.toList()
    }

    fun stats(): String {
        return """
            Nr of records handled: $recordsHandled. 
            Nr of attendeesCreated: ${attendees.size}. 
            Nr of users created: ${users.size}"
        """.trimIndent()
    }

    override fun build(): List<Attendee> {
        return result ?: run {
            val buildResult = attendees.values.map { it.build() }
            result = buildResult
            buildResult
        }
    }
}
