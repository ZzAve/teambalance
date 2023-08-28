package nl.jvandis.teambalance.testdata

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.games.cluedoLocations
import io.kotest.property.arbs.games.cluedoSuspects
import io.kotest.property.arbs.geo.country
import io.kotest.property.arbs.movies.harryPotterCharacter
import io.kotest.property.arbs.products.googleTaxonomy
import io.kotest.property.arbs.stockExchanges
import io.kotest.property.arbs.travel.airport
import io.kotest.property.arbs.tube.tubeJourney
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.jvandis.teambalance.testdata.domain.Attendee
import nl.jvandis.teambalance.testdata.domain.Availability
import nl.jvandis.teambalance.testdata.domain.CreateAttendee
import nl.jvandis.teambalance.testdata.domain.CreateMatch
import nl.jvandis.teambalance.testdata.domain.CreateMiscEvent
import nl.jvandis.teambalance.testdata.domain.CreateTraining
import nl.jvandis.teambalance.testdata.domain.CreateUser
import nl.jvandis.teambalance.testdata.domain.EventResponse
import nl.jvandis.teambalance.testdata.domain.Match
import nl.jvandis.teambalance.testdata.domain.MiscEvent
import nl.jvandis.teambalance.testdata.domain.Place
import nl.jvandis.teambalance.testdata.domain.Role.COACH
import nl.jvandis.teambalance.testdata.domain.Role.DIAGONAL
import nl.jvandis.teambalance.testdata.domain.Role.LIBERO
import nl.jvandis.teambalance.testdata.domain.Role.MID
import nl.jvandis.teambalance.testdata.domain.Role.OTHER
import nl.jvandis.teambalance.testdata.domain.Role.PASSER
import nl.jvandis.teambalance.testdata.domain.Role.SETTER
import nl.jvandis.teambalance.testdata.domain.Role.TRAINER
import nl.jvandis.teambalance.testdata.domain.Training
import nl.jvandis.teambalance.testdata.domain.User
import nl.jvandis.teambalance.testdata.domain.Users
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.time.measureTime

// Enable me if you want to populate the database on application startup
// @Configuration
// @Profile("dev", "local") // don't use class unless 'dev' profile is activated

val jsonFormatter = Json {
    encodeDefaults = true
    isLenient = true
    ignoreUnknownKeys = true
//    prettyPrint = true
}

fun addHeaders(apiKey: String) = Filter { next ->
    {
        it
            .header("X-Secret", apiKey)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .let(next)
    }
}

class Initializer(
    apiKey: String,
    private val host: String = "http://localhost:8080"
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client: HttpHandler = addHeaders(apiKey)(ApacheClient())

    init {
        // standard client
    }

    val trainingsLens = Body.auto<EventResponse<Training>>().toLens()
    val aTrainingLens = Body.auto<Training>().toLens()
    val matchesLens = Body.auto<EventResponse<Match>>().toLens()
    val aMatchLens = Body.auto<Match>().toLens()
    val aMiscEvent = Body.auto<EventResponse<MiscEvent>>().toLens()
    val anAttendeeLens = Body.auto<Attendee>().toLens()

    private fun createUser(user: CreateUser): User {
        val request = Request(POST, "$host/api/users")
            .body(jsonFormatter.encodeToString(user))
        val response: Response = client(request)
        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return jsonFormatter.decodeFromString<User>(response.bodyString())
    }

    private fun getAllUsers(): List<User> {
        val request = Request(GET, "$host/api/users")
        val response: Response = client(request)
        if (!response.status.successful) {
            throw IllegalStateException("Something went fetching users: ${response.bodyString()}")
        }

        return jsonFormatter.decodeFromString<Users>(response.bodyString()).users
    }

    private fun createTraining(training: CreateTraining): EventResponse<Training> {
        val request = Request(POST, "$host/api/trainings")
            .body(jsonFormatter.encodeToString(training))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a training: [${response.status}] ${response.bodyString()}")
        }

        return trainingsLens(response)
    }

    private fun addTrainer(id: Long, trainerId: Long?): Training {
        val request = Request(PUT, "$host/api/trainings/$id/trainer")
            .body("""{ "userId":$trainerId  }""")

        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong adding a trainer: [${response.status}] ${response.bodyString()}")
        }

        return aTrainingLens(response)
    }

    private fun createMatch(match: CreateMatch): EventResponse<Match> {
        val request = Request(POST, "$host/api/matches")
            .body(jsonFormatter.encodeToString(match))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a match: [${response.status}] ${response.bodyString()}")
        }

        return matchesLens(response)
    }

    private fun addCoach(id: Long, coach: String): Match {
        val request = Request(PUT, "$host/api/matches/$id/coach")
            .body("""{ "coach": "$coach"  }""")

        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong adding a coach: [${response.status}] ${response.bodyString()}")
        }

        return aMatchLens(response)
    }

    private fun createMiscEvent(miscEvent: CreateMiscEvent): EventResponse<MiscEvent> {
        val request = Request(POST, "$host/api/miscellaneous-events")
            .body(jsonFormatter.encodeToString(miscEvent))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a misc event: [${response.status}] ${response.bodyString()}")
        }

        return aMiscEvent(response)
    }

    private fun createAttendee(attendee: CreateAttendee): Attendee {
        val request = Request(POST, "$host/api/attendees")
            .body(jsonFormatter.encodeToString(attendee))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return anAttendeeLens(response)
    }

    fun spawnData(config: SpawnDataConfig) {
        createUsers()
        log.info("All users in the system: ")
        val allUsers = getAllUsers()
        log.info("All users: {}", allUsers)

        measureTime { addTrainings(allUsers, config.amountOfTrainings) }
            .also { log.info("Created ${config.amountOfTrainings} in $it") }
        measureTime { addMatches(allUsers, config.amountOfMatches) }
            .also { log.info("Created ${config.amountOfMatches} matches in $it") }
        measureTime { addEvents(allUsers, config.amountOfEvents) }
            .also { log.info("Created ${config.amountOfEvents} misc events in $it") }

//        bankAccountAliasRepository.insertMany(
//            listOf(
//                BankAccountAlias("J. van Dis", allUsers.first { it.name == "Julius" }),
//                BankAccountAlias("J. Post", allUsers.first { it.name == "Bocaj" }),
//                BankAccountAlias("Hr E. Fens", allUsers.first { it.name == "Maurice" }),
//                BankAccountAlias("M.A. Haga", allUsers.first { it.name == "Joep" })
//            )
//        )
//
//        log.info("After alias injection")
//        val aliases = bankAccountAliasRepository.findAll()
//        log.info("All aliases: {}", aliases)
//
//        bankAccountTransactionExclusionRepository.insertMany(
//            listOf(
//                TransactionExclusion(counterParty = "CCV*BUITEN IN DE KUIL")
//            )
//        )
    }

    private fun createUsers() {
        val users = mutableListOf<User>()
        val usersToCreate = listOf(
            CreateUser("Maurice", COACH),
            CreateUser("Pardoes", TRAINER),
            CreateUser("Julius", SETTER),
            CreateUser("Joep", PASSER),
            CreateUser("Roger", PASSER),
            CreateUser("JanPL", PASSER),
            CreateUser("PietPl", PASSER),
            CreateUser("Bocaj", MID),
            CreateUser("HenkMid", MID),
            CreateUser("KeesMid", MID),
            CreateUser("FlipLib", LIBERO),
            CreateUser("MaartenDia", DIAGONAL),
            CreateUser("DriesDia", DIAGONAL),
            CreateUser("Tjapko", OTHER),
        )

        usersToCreate.forEach {
            log.info("Creating user ${it.name}")
            val result: Result<User> = kotlin.runCatching {
                createUser(it)
            }

            if (result.isSuccess) {
                val user = result.getOrNull() ?: error("Shouldn't be null")
                log.info("Created user ${user.name} [${user.role}] with id ${user.id}")
                users.add(user)
            } else {
                log.error(
                    "Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}",
                    result.exceptionOrNull()
                )
            }
        }
    }

    private fun addTrainings(allUsers: List<User>, amountOfEvents: Int) {
        val locations = Arb.cluedoLocations().take(10).map { it.name }.toList()
        val comments = Arb.stockExchanges().take(2).map { it.name }.toList() + null

        log.info(
            """
            .
            .
            .
            Adding $amountOfEvents trainings to system.
            Locations to use: $locations
            Comments to use: $comments
            Day range: +- 100 days
            Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent()
        )

        val dayRange = 100L
        val hourRange = 100L
        (0..amountOfEvents).forEach { i ->
            try {
                log.info("Creating Training $i")
                val training = CreateTraining(
                    startTime = LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(Random.nextLong(-dayRange, dayRange))
                        .plusHours(Random.nextLong(-hourRange, hourRange))
                        .toKotlinLocalDateTime(),
                    location = locations.random(),
                    comment = comments.random()
                )

                val savedTraining = createTraining(training).events.first()
                log.info("Added training with id ${savedTraining.id}: $savedTraining")

                val addedAttendees = addAttendeesToEvent(allUsers, savedTraining.id)
                log.info("Added attendees to training with id ${savedTraining.id}: ${addedAttendees.map { it.user.name }}")

                // Add trainer, 50% chance
                if (Random.nextBoolean()) {
                    val trainerId = addedAttendees
                        .take(1)
                        .map { it.user.id }
                        .firstOrNull()
                    val updatedTraining = addTrainer(savedTraining.id, trainerId)

                    log.info("Added training to training with id ${savedTraining.id}: ${updatedTraining.trainer}")
                } else {
                    log.info("No trainer will be added to training with id ${savedTraining.id}")
                }
            } catch (e: Exception) {
                log.warn("Could not add training $i. continuing with the rest", e)
            }
        }

        log.info("Done adding $amountOfEvents trainings")
    }

    private fun addMatches(allUsers: List<User>, amountOfEvents: Int) {
        val locations = Arb.airport().take(10).map { "${it.name} - ${it.country}" }.toList()
        val comments = Arb.googleTaxonomy().take(4).map { it.value }.toList() + null
        val opponents = Arb.cluedoSuspects().take(10).map { it.name }.toList()
        val coaches = Arb.harryPotterCharacter().take(5).map { "${it.firstName} ${it.lastName}" }.toList()
        log.info(
            """
            .
            .
            .
            Adding $amountOfEvents matches to system.
            Locations to use: $locations
            Comments to use: $comments
            Opponents to use: $opponents
            Coaches to use: $coaches
            Day range: +- 100 days
            Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent()
        )

        val dayRange = 100L
        val hourRange = 100L
        (0..amountOfEvents).forEach { i ->
            try {
                log.info("Creating match $i")
                val match = CreateMatch(
                    startTime = LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(Random.nextLong(-dayRange, dayRange))
                        .plusHours(Random.nextLong(-hourRange, hourRange))
                        .toKotlinLocalDateTime(),
                    location = locations.random(),
                    opponent = opponents.random(),
                    homeAway = Place.entries.random(),
                    comment = comments.random()
                )

                val savedMatch = createMatch(match).events.first()
                log.info("Added match with id ${savedMatch.id}: $savedMatch")

                val addedAttendees = addAttendeesToEvent(allUsers, savedMatch.id)

                log.info("Added attendees to match with id ${savedMatch.id}: ${addedAttendees.map { it.user.name }}")

                // Add coach, 50% chance
                if (Random.nextBoolean()) {
                    val coachToAdd = coaches.random()
                    val updatedMatch = addCoach(savedMatch.id, coachToAdd)

                    log.info("Added coach to match with id ${savedMatch.id}: ${updatedMatch.coach}")
                } else {
                    log.info("No coach will be added to match with id ${savedMatch.id}")
                }
            } catch (e: Exception) {
                log.warn("Could not add match $i. continuing with the rest", e)
            }
        }

        log.info("Done adding $amountOfEvents matches")
    }

    private fun addEvents(allUsers: List<User>, amountOfEvents: Int) {
        val locations = Arb.country().take(10).map { it.name }.toList()
        val comments = Arb.googleTaxonomy().take(4).map { it.value }.toList() + null
        val titles = Arb.tubeJourney().take(5).map { "Travel from ${it.start.name} tot ${it.end.name}" }.toList()
        log.info(
            """
            .
            .
            .
            Adding $amountOfEvents misc events to system.
                Locations to use: $locations
                Comments to use: $comments
                Titles to use: $titles
                Day range: +- 100 days
                Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent()
        )

        val dayRange = 100L
        val hourRange = 100L
        (0..amountOfEvents).forEach { i ->
            try {
                log.info("Creating match $i")
                val miscEvent = CreateMiscEvent(
                    startTime = LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(Random.nextLong(-dayRange, dayRange))
                        .plusHours(Random.nextLong(-hourRange, hourRange))
                        .toKotlinLocalDateTime(),
                    location = locations.random(),
                    title = titles.random(),
                    comment = comments.random()
                )

                val savedMiscEvent = createMiscEvent(miscEvent).events.first()
                log.info("Added misc event with id ${savedMiscEvent.id}: $savedMiscEvent")

                val addedAttendees = addAttendeesToEvent(allUsers, savedMiscEvent.id)
                log.info("Added attendees to match with id ${savedMiscEvent.id}: ${addedAttendees.map { it.user.name }}")
            } catch (e: Exception) {
                log.warn("Could not add match $i. continuing with the rest", e)
            }
        }

        log.info("Done adding $amountOfEvents misc events")
    }

    private fun addAttendeesToEvent(
        allUsers: List<User>,
        eventId: Long
    ): MutableList<Attendee> {
        val addedAttendees = mutableListOf<Attendee>()
        // Add subset of allUsers as attendee
        allUsers.shuffled().take(Random.nextInt(allUsers.size))
            .map { user ->
                CreateAttendee(
                    userId = user.id,
                    eventId = eventId,
                    availability = Availability.values()[Random.nextInt(Availability.values().size)]
                )
            }
            .forEach {
                val result = kotlin.runCatching { createAttendee(it) }
                if (result.isSuccess) {
                    val attendee = result.getOrNull() ?: error("Shouldn't be null")
                    log.debug("Created attendee ${attendee.user.id} [${attendee.state}] for event with id ${attendee.eventId}")
                    addedAttendees.add(attendee)
                } else {
                    log.error("Could not create attendee for userId ${it.userId}", result.exceptionOrNull())
                }
            }
        return addedAttendees
    }
}

data class SpawnDataConfig(
    val amountOfTrainings: Int,
    val amountOfMatches: Int,
    val amountOfEvents: Int
)
