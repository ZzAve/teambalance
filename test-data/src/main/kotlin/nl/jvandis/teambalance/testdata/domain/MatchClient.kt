package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.games.cluedoSuspects
import io.kotest.property.arbs.movies.harryPotterCharacter
import io.kotest.property.arbs.products.googleTaxonomy
import io.kotest.property.arbs.travel.airport
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.EventCreationException
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

private const val MATCHES_BASE_URL = "/api/matches"

class MatchClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
    private val attendeeClient: AttendeeClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val matchesLens = Body.auto<Event<Match>>().toLens()
    val aMatchLens = Body.auto<Match>().toLens()

    private fun createMatch(match: CreateMatch): Event<Match> {
        val request = Request(Method.POST, MATCHES_BASE_URL).body(jsonFormatter.encodeToString(match))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a match: [${response.status}] ${response.bodyString()}"
        }

        return matchesLens(response)
    }

    private fun addCoach(
        id: String,
        additionalInfo: String,
    ): Match {
        val request =
            Request(Method.PUT, "/api/matches/$id/additional-info").body("""{ "additionalInfo": "$additionalInfo"  }""")

        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong updating additional-info: [${response.status}] ${response.bodyString()}"
        }
        return aMatchLens(response)
    }

    fun addMatches(allUsers: List<User>): List<Match> {
        val locations =
            Arb
                .airport()
                .take(10, RandomSource(random, random.nextLong()))
                .map { "${it.name} - ${it.country}" }
                .toList()
        val comments =
            Arb
                .googleTaxonomy()
                .take(4, RandomSource(random, random.nextLong()))
                .map { it.value }
                .toList() + null
        val opponents =
            Arb
                .cluedoSuspects()
                .take(10, RandomSource(random, random.nextLong()))
                .map { it.name }
                .toList()
        val additionalInfo =
            Arb
                .harryPotterCharacter()
                .take(5, RandomSource(random, random.nextLong()))
                .map { "${it.firstName} ${it.lastName}" }
                .toList()
        log.info(
            """
            .
            .
            .
            Adding ${config.amountOfMatches} matches to system.
            Locations to use: $locations
            Comments to use: $comments
            Opponents to use: $opponents
            AddionalInfo to use: $additionalInfo
            Day range: +- 100 days
            Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent(),
        )

        val dayRange = 100L
        val hourRange = 100L
        val createdMatches =
            (0 until config.amountOfMatches).map { i ->
                try {
                    log.info("Creating match $i")
                    val match =
                        CreateMatch(
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
                            opponent = opponents.random(),
                            homeAway = Place.entries.random(),
                            comment = comments.random(),
                        )

                    val createdMatch = createMatch(match).events.first()
                    log.info("Added match with id ${createdMatch.id}: $createdMatch")

                    val addedAttendees = attendeeClient.createAndValidateAttendees(allUsers, createdMatch.id)

                    log.info("Added attendees to match with id ${createdMatch.id}: ${addedAttendees.map { it.user.name }}")

                    // Add coach, 50% chance
                    if (random.nextBoolean()) {
                        val coachToAdd = additionalInfo.random()
                        val updatedMatch = addCoach(createdMatch.id, coachToAdd)

                        log.info("Added additonal info to match with id ${createdMatch.id}: ${updatedMatch.additionalInfo}")
                    } else {
                        log.info("No additionalInfo will be added to match with id ${createdMatch.id}")
                    }
                    createdMatch
                } catch (e: Exception) {
                    throw EventCreationException("Could not add match $i", e)
                }
            }

        log.info("Done adding ${config.amountOfMatches} matches")
        return createdMatches
    }
}
