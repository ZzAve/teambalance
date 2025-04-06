package nl.jvandis.teambalance.testdata.domain

import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.AttendeeCreationException
import nl.jvandis.teambalance.testdata.jsonFormatter
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import kotlin.random.Random

private const val ATTENDEE_BASE_URL = "/api/attendees"

class AttendeeClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val anAttendeeLens = Body.auto<Attendee>().toLens()
    private val anAttendeesLens = Body.auto<Attendees>().toLens()

    fun createAndValidateAttendees(
        allUsers: List<User>,
        eventId: String,
    ): List<Attendee> {
        val addedAttendees = mutableListOf<Attendee>()

        // Add subset of allUsers as attendee
        allUsers
            .shuffled()
            .take(random.nextInt(allUsers.size))
            .map { user ->
                CreateAttendee(
                    userId = user.id,
                    eventId = eventId,
                    availability = Availability.entries[random.nextInt(Availability.entries.size)],
                )
            }.forEach {
                val createdAttendee = createAttendee(it)
                log.info(
                    "Created attendee {} [{}] for event with id {}",
                    createdAttendee.user.id,
                    createdAttendee.state,
                    createdAttendee.eventId,
                )

                val fetchedAttendee = getAttendee(createdAttendee.id)
                if (createdAttendee != fetchedAttendee) {
                    throw AttendeeCreationException(
                        "Created attendee cannot be fetched. It seems something is wrong with the database. " +
                            "Created alias: -- $createdAttendee --, fetched alias: -- $fetchedAttendee --",
                    )
                }
                addedAttendees.add(createdAttendee)
            }

        val allAttendees = getAllAttendees(listOf(eventId))
        if (!allAttendees.containsAll(addedAttendees)) {
            throw AttendeeCreationException(
                "Not all attendees were created. Created: $addedAttendees, all: $allAttendees" +
                    "Missing attendees for event $eventId: ${addedAttendees.filter { !allAttendees.contains(it) }}",
            )
        }

        return addedAttendees
    }

    private fun createAttendee(attendee: CreateAttendee): Attendee {
        val request = Request(POST, "$ATTENDEE_BASE_URL").body(jsonFormatter.encodeToString(attendee))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating an attendee: ${response.bodyString()}"
        }

        return anAttendeeLens(response)
    }

    private fun getAttendee(attendeeId: String): Attendee {
        val request = Request(GET, "$ATTENDEE_BASE_URL/$attendeeId")
        val response: Response = client(request)

        if (!response.status.successful) {
            throw AttendeeCreationException(
                "Failed to get attendee with ID: $attendeeId. Status: ${response.status}",
            )
        }

        return anAttendeeLens.extract(response)
    }

    fun getAllAttendees(eventIds: List<String>?): List<Attendee> {
        val request = Request(GET, ATTENDEE_BASE_URL).query("event-ids", (eventIds ?: emptyList()).joinToString(","))
        val response = client(request)

        if (!response.status.successful) {
            throw AttendeeCreationException("Failed to get all attendees. Status: ${response.status}")
        }

        return anAttendeesLens.extract(response).attendees
    }
}
