package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.geo.country
import io.kotest.property.arbs.products.googleTaxonomy
import io.kotest.property.arbs.tube.tubeJourney
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.jsonFormatter
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.random.Random

private const val MISC_EVENTS_BASE_URL = "/api/miscellaneous-events"

class EventClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
    private val attendeeClient: AttendeeClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val aMiscEvent = Body.auto<Event<MiscEvent>>().toLens()

    private fun createMiscEvent(miscEvent: CreateMiscEvent): Event<MiscEvent> {
        val request = Request(Method.POST, MISC_EVENTS_BASE_URL).body(jsonFormatter.encodeToString(miscEvent))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a misc event: [${response.status}] ${response.bodyString()}"
        }
        return aMiscEvent(response)
    }

    fun addEvents(allUsers: List<User>) {
        val locations =
            Arb
                .country()
                .take(10)
                .map { it.name }
                .toList()
        val comments =
            Arb
                .googleTaxonomy()
                .take(4)
                .map { it.value }
                .toList() + null
        val titles =
            Arb
                .tubeJourney()
                .take(5)
                .map { "Travel from ${it.start.name} tot ${it.end.name}" }
                .toList()
        log.info(
            """
            .
            .
            .
            Adding ${config.amountOfEvents} misc events to system.
                Locations to use: $locations
                Comments to use: $comments
                Titles to use: $titles
                Day range: +- 100 days
                Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent(),
        )

        val dayRange = 100L
        val hourRange = 100L
        (0 until config.amountOfEvents).forEach { i ->
            try {
                log.info("Creating match $i")
                val miscEvent =
                    CreateMiscEvent(
                        startTime =
                            LocalDateTime
                                .now()
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .plusDays(random.nextLong(-dayRange, dayRange))
                                .plusHours(random.nextLong(-hourRange, hourRange))
                                .toKotlinLocalDateTime(),
                        location = locations.random(),
                        title = titles.random(),
                        comment = comments.random(),
                    )

                val savedMiscEvent: MiscEvent = createMiscEvent(miscEvent).events.first()
                log.info("Added misc event with id ${savedMiscEvent.id}: $savedMiscEvent")

                val addedAttendees = attendeeClient.createAndValidateAttendees(allUsers, savedMiscEvent.id)
                log.info("Added attendees to match with id ${savedMiscEvent.id}: ${addedAttendees.map { it.user.name }}")
            } catch (e: Exception) {
                log.warn("Could not add match $i. continuing with the rest", e)
                throw CouldNotCreateEntityException.EventCreationException("Could not add event $i", e)
            }
        }

        log.info("Done adding ${config.amountOfEvents} misc events")
    }
}
