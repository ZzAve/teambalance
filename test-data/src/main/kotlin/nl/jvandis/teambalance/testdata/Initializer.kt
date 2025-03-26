package nl.jvandis.teambalance.testdata

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.firstName
import io.kotest.property.arbs.games.cluedoLocations
import io.kotest.property.arbs.games.cluedoSuspects
import io.kotest.property.arbs.geo.country
import io.kotest.property.arbs.movies.harryPotterCharacter
import io.kotest.property.arbs.name
import io.kotest.property.arbs.products.googleTaxonomy
import io.kotest.property.arbs.stockExchanges
import io.kotest.property.arbs.travel.airport
import io.kotest.property.arbs.tube.tubeJourney
import io.kotest.property.arbs.usernames
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.jvandis.teambalance.testdata.domain.Attendee
import nl.jvandis.teambalance.testdata.domain.Availability
import nl.jvandis.teambalance.testdata.domain.BankAccountAlias
import nl.jvandis.teambalance.testdata.domain.BankAccountAliases
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.AliasCreationException
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.EventCreationException
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.TransactionExclusionCreationException
import nl.jvandis.teambalance.testdata.domain.CreateAttendee
import nl.jvandis.teambalance.testdata.domain.CreateBankAccountAlias
import nl.jvandis.teambalance.testdata.domain.CreateMatch
import nl.jvandis.teambalance.testdata.domain.CreateMiscEvent
import nl.jvandis.teambalance.testdata.domain.CreateTraining
import nl.jvandis.teambalance.testdata.domain.CreateTransactionExclusion
import nl.jvandis.teambalance.testdata.domain.CreateUser
import nl.jvandis.teambalance.testdata.domain.EventResponse
import nl.jvandis.teambalance.testdata.domain.Match
import nl.jvandis.teambalance.testdata.domain.MiscEvent
import nl.jvandis.teambalance.testdata.domain.Place
import nl.jvandis.teambalance.testdata.domain.Role
import nl.jvandis.teambalance.testdata.domain.Training
import nl.jvandis.teambalance.testdata.domain.TransactionExclusion
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

val jsonFormatter =
    Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
//    prettyPrint = true
    }

fun addHeaders(apiKey: String) =
    Filter { next ->
        {
            it
                .header("X-Secret", apiKey)
                .header("Content-Type", ContentType.APPLICATION_JSON.value)
                .let(next)
        }
    }

class Initializer(
    apiKey: String,
    private val host: String = "http://localhost:8080",
    private val random: Random,
    private val config: SpawnDataConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client: HttpHandler = addHeaders(apiKey)(ApacheClient())

    val trainingsLens = Body.auto<EventResponse<Training>>().toLens()
    val aTrainingLens = Body.auto<Training>().toLens()
    val matchesLens = Body.auto<EventResponse<Match>>().toLens()
    val aMatchLens = Body.auto<Match>().toLens()
    val aMiscEvent = Body.auto<EventResponse<MiscEvent>>().toLens()
    val anAttendeeLens = Body.auto<Attendee>().toLens()
    val aBankAccountAliasLens = Body.auto<BankAccountAlias>().toLens()
    val aTransactionExclusion = Body.auto<TransactionExclusion>().toLens()

    private fun createUser(user: CreateUser): User {
        val body = jsonFormatter.encodeToString(user)
        val request =
            Request(POST, "$host/api/users")
                .body(body)
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went wrong creating a user: ${response.bodyString()}"
        }

        return jsonFormatter.decodeFromString<User>(response.bodyString())
    }

    private fun getAllUsers(): List<User> {
        val request = Request(GET, "$host/api/users")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went fetching users: ${response.bodyString()}"
        }

        return jsonFormatter.decodeFromString<Users>(response.bodyString()).users
    }

    private fun createTraining(training: CreateTraining): EventResponse<Training> {
        val request =
            Request(POST, "$host/api/trainings")
                .body(jsonFormatter.encodeToString(training))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a training: [${response.status}] ${response.bodyString()}"
        }

        return trainingsLens(response)
    }

    private fun addTrainer(
        id: String,
        trainerId: String?,
    ): Training {
        val request =
            Request(PUT, "$host/api/trainings/$id/trainer")
                .body(
                    trainerId
                        ?.let { """{ "userId": "$it"  }""" }
                        ?: "",
                )

        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong adding a trainer: [${response.status}] ${response.bodyString()}"
        }

        return aTrainingLens(response)
    }

    private fun createMatch(match: CreateMatch): EventResponse<Match> {
        val request =
            Request(POST, "$host/api/matches")
                .body(jsonFormatter.encodeToString(match))
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
            Request(PUT, "$host/api/matches/$id/additional-info")
                .body("""{ "additionalInfo": "$additionalInfo"  }""")

        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong updating additional-info: [${response.status}] ${response.bodyString()}"
        }
        return aMatchLens(response)
    }

    private fun createMiscEvent(miscEvent: CreateMiscEvent): EventResponse<MiscEvent> {
        val request =
            Request(POST, "$host/api/miscellaneous-events")
                .body(jsonFormatter.encodeToString(miscEvent))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a misc event: [${response.status}] ${response.bodyString()}"
        }
        return aMiscEvent(response)
    }

    private fun createAttendee(attendee: CreateAttendee): Attendee {
        val request =
            Request(POST, "$host/api/attendees")
                .body(jsonFormatter.encodeToString(attendee))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a user: ${response.bodyString()}"
        }

        return anAttendeeLens(response)
    }

    private fun getAllAliases(): List<BankAccountAlias> {
        val request = Request(GET, "$host/api/aliases")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went fetching aliases: ${response.bodyString()}"
        }

        return jsonFormatter.decodeFromString<BankAccountAliases>(response.bodyString()).bankAccountAliases
    }

    private fun createAlias(bankAccountAlias: CreateBankAccountAlias): BankAccountAlias {
        val request =
            Request(POST, "$host/api/aliases")
                .body(jsonFormatter.encodeToString(bankAccountAlias))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a alias: ${response.bodyString()}"
        }

        return aBankAccountAliasLens(response)
    }

    private fun createTransactionExclusion(createTransactionExclusion: CreateTransactionExclusion): TransactionExclusion {
        val request =
            Request(POST, "$host/api/transaction-exclusions")
                .body(jsonFormatter.encodeToString(createTransactionExclusion))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a transactionExclusion: ${response.bodyString()}"
        }

        return aTransactionExclusion(response)
    }

    fun spawnData() {
        measureTime { createUsers() }.also { log.info("Created ${config.amountOfMatches} users in $it") }
        log.info("All users in the system: ")
        val allUsers = getAllUsers()
        log.info("All users: {}", allUsers)

        measureTime { addTrainings(allUsers) }
            .also { log.info("Created ${config.amountOfTrainings} trainings in $it") }
        measureTime { addMatches(allUsers) }
            .also { log.info("Created ${config.amountOfMatches} matches in $it") }
        measureTime { addEvents(allUsers) }
            .also { log.info("Created ${config.amountOfEvents} misc events in $it") }
        measureTime { createBankAccountAliases(allUsers) }
            .also { log.info("Created ${config.amountOfAliases} aliases in $it") }
        measureTime { createTransactionExclusions() }
            .also { log.info("Created ${config.amountOfTransactionExclusions} transaction exclusions in $it") }
    }

    private fun createTransactionExclusions() {
        (0..config.amountOfTransactionExclusions).forEach { i ->
            val date = if (random.nextDouble(1.0) < .1) java.time.LocalDate.now().toKotlinLocalDate() else null
            val transactionId = if (random.nextDouble(1.0) < .3) random.nextInt(5_000, 10_000) else null
            val counterParty = if (random.nextDouble(1.0) < .3) Arb.name().take(1).first().let { "${it.first} ${it.last}" } else null
            val createTransactionExclusion =
                CreateTransactionExclusion(
                    date = date,
                    transactionId = transactionId,
                    counterParty = counterParty,
                    description = if (date == null && transactionId == null && counterParty == null) "Description $i" else null,
                )
            try {
                log.info("Creating transactionExclusion $createTransactionExclusion")
                createTransactionExclusion(createTransactionExclusion)
            } catch (e: RuntimeException) {
                if (config.strictMode) {
                    throw TransactionExclusionCreationException(
                        "Could not add transactionExclusion $createTransactionExclusion",
                        e,
                    )
                }
                log.error("Could not add transactionExclusion $createTransactionExclusion", e)
                null
            }
        }
    }

    private fun createBankAccountAliases(allUsers: List<User>) {
        val bankAccountAliases: List<BankAccountAlias> =
            Arb.usernames().take(config.amountOfAliases).map {
                CreateBankAccountAlias(it.value, allUsers.random(random).id)
            }
                .mapNotNull {
                    try {
                        log.info("Creating BankAccountAlias $it")
                        createAlias(it)
                    } catch (e: RuntimeException) {
                        if (config.strictMode) {
                            throw AliasCreationException("Could not add bankAccountAlias ${it.alias}", e)
                        }
                        log.error("Could not add bankAccountAlias ${it.alias}", e)
                        null
                    }
                }.toList()

        log.info("All injected aliases: {}", bankAccountAliases)
        log.info("All aliases: {} ", getAllAliases().map { "Alias '${it.alias}' for user ${it.user.name} (${it.user.id})" })
    }

    private fun createUsers() {
        val users = mutableListOf<User>()
        val usersToCreate =
            Arb.firstName().take(config.amountOfUsers).map { it.name }.map {
                CreateUser(it, Role.entries.random(random))
            }.toList()

        usersToCreate.forEach {
            log.info("Creating user ${it.name} with role ${it.role}")
            val result: Result<User> =
                kotlin.runCatching {
                    createUser(it)
                }

            if (result.isSuccess) {
                val user = result.getOrNull() ?: error("Shouldn't be null")
                log.info("Created user ${user.name} with role ${user.role}. Id ${user.id}")
                users.add(user)
            } else {
                log.error(
                    "Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}",
                    result.exceptionOrNull(),
                )
                if (config.strictMode) {
                    throw CouldNotCreateEntityException.UserCreationException(
                        "Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}",
                        result.exceptionOrNull(),
                    )
                }
            }
        }
    }

    private fun addTrainings(allUsers: List<User>) {
        val locations = Arb.cluedoLocations().take(10).map { it.name }.toList()
        val comments = Arb.stockExchanges().take(2).map { it.name }.toList() + null

        log.info(
            """
            .
            .
            .
            Adding ${config.amountOfTrainings} trainings to system.
            Locations to use: $locations
            Comments to use: $comments
            Day range: +- 100 days
            Hour range: +- 100 hours
            .
            .
            .
            """.trimIndent(),
        )

        val dayRange = 100L
        val hourRange = 100L
        (0..config.amountOfTrainings).forEach { i ->
            try {
                log.info("Creating Training $i")
                val training =
                    CreateTraining(
                        startTime =
                            LocalDateTime.now()
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .plusDays(random.nextLong(-dayRange, dayRange))
                                .plusHours(random.nextLong(-hourRange, hourRange))
                                .toKotlinLocalDateTime(),
                        location = locations.random(),
                        comment = comments.random(),
                    )

                val savedTraining = createTraining(training).events.first()
                log.info("Added training with id ${savedTraining.id}: $savedTraining")

                val addedAttendees = addAttendeesToEvent(allUsers, savedTraining.id)
                log.info("Added attendees to training with id ${savedTraining.id}: ${addedAttendees.map { it.user.name }}")

                // Add trainer, 50% chance
                if (random.nextBoolean() && addedAttendees.isNotEmpty()) {
                    val trainerId =
                        addedAttendees
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
                if (config.strictMode) {
                    throw EventCreationException("Could not add training $i", e)
                }
            }
        }

        log.info("Done adding ${config.amountOfTrainings} trainings")
    }

    private fun addMatches(allUsers: List<User>) {
        val locations = Arb.airport().take(10).map { "${it.name} - ${it.country}" }.toList()
        val comments = Arb.googleTaxonomy().take(4).map { it.value }.toList() + null
        val opponents = Arb.cluedoSuspects().take(10).map { it.name }.toList()
        val additionalInfo = Arb.harryPotterCharacter().take(5).map { "${it.firstName} ${it.lastName}" }.toList()
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
        (0..config.amountOfMatches).forEach { i ->
            try {
                log.info("Creating match $i")
                val match =
                    CreateMatch(
                        startTime =
                            LocalDateTime.now()
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

                val savedMatch: Match = createMatch(match).events.first()
                log.info("Added match with id ${savedMatch.id}: $savedMatch")

                val addedAttendees = addAttendeesToEvent(allUsers, savedMatch.id)

                log.info("Added attendees to match with id ${savedMatch.id}: ${addedAttendees.map { it.user.name }}")

                // Add coach, 50% chance
                if (random.nextBoolean()) {
                    val coachToAdd = additionalInfo.random()
                    val updatedMatch = addCoach(savedMatch.id, coachToAdd)

                    log.info("Added additonal info to match with id ${savedMatch.id}: ${updatedMatch.additionalInfo}")
                } else {
                    log.info("No additionalInfo will be added to match with id ${savedMatch.id}")
                }
            } catch (e: Exception) {
                log.warn("Could not add match $i. continuing with the rest", e)
                if (config.strictMode) {
                    throw EventCreationException("Could not add match $i", e)
                }
            }
        }

        log.info("Done adding ${config.amountOfMatches} matches")
    }

    private fun addEvents(allUsers: List<User>) {
        val locations = Arb.country().take(10).map { it.name }.toList()
        val comments = Arb.googleTaxonomy().take(4).map { it.value }.toList() + null
        val titles = Arb.tubeJourney().take(5).map { "Travel from ${it.start.name} tot ${it.end.name}" }.toList()
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
        (0..config.amountOfEvents).forEach { i ->
            try {
                log.info("Creating match $i")
                val miscEvent =
                    CreateMiscEvent(
                        startTime =
                            LocalDateTime.now()
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

                val addedAttendees = addAttendeesToEvent(allUsers, savedMiscEvent.id)
                log.info("Added attendees to match with id ${savedMiscEvent.id}: ${addedAttendees.map { it.user.name }}")
            } catch (e: Exception) {
                log.warn("Could not add match $i. continuing with the rest", e)
                if (config.strictMode) {
                    throw EventCreationException("Could not add event $i", e)
                }
            }
        }

        log.info("Done adding ${config.amountOfEvents} misc events")
    }

    private fun addAttendeesToEvent(
        allUsers: List<User>,
        eventId: String,
    ): MutableList<Attendee> {
        val addedAttendees = mutableListOf<Attendee>()
        // Add subset of allUsers as attendee

        allUsers.shuffled().take(random.nextInt(allUsers.size))
            .map { user ->
                CreateAttendee(
                    userId = user.id,
                    eventId = eventId,
                    availability = Availability.entries[random.nextInt(Availability.entries.size)],
                )
            }
            .forEach {
                val result = kotlin.runCatching { createAttendee(it) }
                if (result.isSuccess) {
                    val attendee = result.getOrNull() ?: error("Shouldn't be null")
                    log.debug(
                        "Created attendee {} [{}] for event with id {}",
                        attendee.user.id,
                        attendee.state,
                        attendee.eventId,
                    )
                    addedAttendees.add(attendee)
                } else {
                    log.error("Could not create attendee for userId ${it.userId}", result.exceptionOrNull())
                }
            }
        return addedAttendees
    }
}

data class SpawnDataConfig(
    val amountOfUsers: Int,
    val amountOfTrainings: Int,
    val amountOfMatches: Int,
    val amountOfEvents: Int,
    val amountOfAliases: Int,
    val amountOfTransactionExclusions: Int,
    val strictMode: Boolean,
)
