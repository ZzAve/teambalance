package nl.jvandis.teambalance.testdata

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.games.cluedoLocations
import io.kotest.property.arbs.stockExchanges
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.jvandis.teambalance.testdata.domain.Attendee
import nl.jvandis.teambalance.testdata.domain.Availability
import nl.jvandis.teambalance.testdata.domain.CreateAttendee
import nl.jvandis.teambalance.testdata.domain.CreateTraining
import nl.jvandis.teambalance.testdata.domain.CreateUser
import nl.jvandis.teambalance.testdata.domain.Role.COACH
import nl.jvandis.teambalance.testdata.domain.Role.DIAGONAL
import nl.jvandis.teambalance.testdata.domain.Role.MID
import nl.jvandis.teambalance.testdata.domain.Role.PASSER
import nl.jvandis.teambalance.testdata.domain.Training
import nl.jvandis.teambalance.testdata.domain.User
import nl.jvandis.teambalance.testdata.domain.Users
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.routing.header
import org.slf4j.LoggerFactory
import kotlin.random.Random

// Enable me if you want to populate the database on application startup
// @Configuration
// @Profile("dev", "local") // don't use class unless 'dev' profile is activated

val jsonFormatter = Json {
    encodeDefaults = true
}

class Initializer(
    private val apiKey: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client: HttpHandler;

    private val host = "http://localhost:8080"

    init {
        // standard client
        client = ApacheClient()
            .apply {
                header("X-Secret", apiKey)
                header("Content-Type", ContentType.APPLICATION_JSON.value)
            }

    }

    val aTrainingLens = Body.auto<Training>().toLens()
    val anAttendeeLens = Body.auto<Attendee>().toLens()

    private fun createUser(user: CreateUser): User {
        val request = Request(POST, "$host/api/users")
            .header("X-Secret", apiKey)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body(jsonFormatter.encodeToString(user))
        val response: Response = client(request)
        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return jsonFormatter.decodeFromString<User>(response.bodyString())
    }

    private fun getAllUsers(): List<User> {
        val request = Request(GET, "$host/api/users")
            .header("X-Secret", apiKey)
        val response: Response = client(request)
        if (!response.status.successful) {
            throw IllegalStateException("Something went fetching users: ${response.bodyString()}")
        }


        return jsonFormatter.decodeFromString<Users>(response.bodyString()).users
    }

    private fun createTraining(training: CreateTraining): Training {
        val request = Request(POST, "$host/api/trainings")
            .header("X-Secret", apiKey)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body(jsonFormatter.encodeToString(training))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return aTrainingLens(response)
    }

    private fun addTrainer(id: Long, trainerId: Long?): Training {
        val request = Request(PUT, "$host/api/trainings/$id/trainer")
            .header("X-Secret", apiKey)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body("""{ "userId":$trainerId  }""")

        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return aTrainingLens(response)
    }


    private fun createAttendee(attendee: CreateAttendee): Attendee {
        val request = Request(POST, "$host/api/attendees")
            .header("X-Secret", apiKey)
            .header("Content-Type", ContentType.APPLICATION_JSON.value)
            .body(jsonFormatter.encodeToString(attendee))
        val response: Response = client(request)

        if (!response.status.successful) {
            throw IllegalStateException("Something went wrong creating a user: ${response.bodyString()}")
        }

        return anAttendeeLens(response)
    }

    fun spawnData(): Unit {
        val users = mutableListOf<User>()
        val usersToCreate = listOf(
            CreateUser("Julius", DIAGONAL),
            CreateUser("Maurice", COACH),
            CreateUser("Bocaj", MID),
            CreateUser("Joep", PASSER),
            CreateUser("Roger", PASSER),
            CreateUser("Pardoes", COACH)
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
                log.error("Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}", result.exceptionOrNull())
            }
        }

        log.info("All users in the system: ")
        val allUsers = getAllUsers()
        log.info("All users: {}", allUsers)

        addTrainings(allUsers, 100)
//        addMatches(allUsers, 10)
//        addEvents(allUsers, 10)

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

    private fun addTrainings(allUsers: List<User>, amountOfEvents: Int) {
        val locations = Arb.cluedoLocations().take(10).toList()
        val comments = Arb.stockExchanges().take(2).toList() + null

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
                    java.time.LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .plusDays(Random.nextLong(-dayRange, dayRange))
                        .plusHours(Random.nextLong(-hourRange, hourRange))
                        .toKotlinLocalDateTime(),
                    locations.random().name,
                    comments.random()?.name
                )

                val savedTraining = createTraining(training)
                log.info("Added training with id ${savedTraining.id}: $savedTraining")

                val addedAttendees = mutableListOf<Attendee>()
                // Add subset of allUsers as attendee
                allUsers.shuffled().take(Random.nextInt(allUsers.size))
                    .map { user ->
                        CreateAttendee(
                            userId = user.id,
                            eventId = savedTraining.id,
                            availability = Availability.values()[Random.nextInt(Availability.values().size)]
                        )
                    }
                    .forEach {
                        val result = kotlin.runCatching { createAttendee(it) }
                        if (result.isSuccess) {
                            val attendee = result.getOrNull() ?: error("Shouldn't be null")
                            log.info("Created attendee ${attendee.user.id} [${attendee.state}] for event with id ${attendee.eventId}")
                            addedAttendees.add(attendee)
                        } else {
                            log.error("Could not create attendee for userId ${it.userId}", result.exceptionOrNull())
                        }
                    }

                log.info("Added attendees to training with id ${savedTraining.id}: ${addedAttendees.map { it.user.name }}")

                //Add trainer, 50% chance
                if (Random.nextBoolean()) {
                    val trainerId = addedAttendees
                        .take(1)
                        .map { it.user.id }
                        .firstOrNull()
                    val (trainer) = addTrainer(savedTraining.id, trainerId)

                    log.info("Added training to training with id ${savedTraining.id}: $trainer")
                } else {
                    log.info("No trainer will be added to training with id ${savedTraining.id}")
                }
            } catch (e: Exception) {
                log.warn("Could not add training $i. continuing with the rest", e)
            }
        }

        log.info("Done adding $amountOfEvents trainings")
    }
//
//    private fun addMatches(allUsers: Iterable<User>, amountOfEvents: Int) {
//        matchRepository.insert(
//            Match(
//                startTime = LocalDateTime.now().minusDays(3),
//                location = "Match plaza",
//                comment = "No, this is patrick"
//            )
//        )
//        matchRepository.insert(
//            Match(
//                startTime = LocalDateTime.now().plusDays(10),
//                location = "123123,asdf",
//                comment = ""
//            )
//        )
//        matchRepository.insert(
//            Match(
//                startTime = LocalDateTime.now().minusDays(20),
//                location = "Match,asdf",
//                comment = ""
//            )
//        )
//        matchRepository.insert(
//            Match(
//                startTime = LocalDateTime.now().plusDays(22),
//                location = "Match,asdf",
//                comment = ""
//            )
//        )
//
//        log.info("After Match injection")
//        val matches = matchRepository.findAll()
//        log.info("All Match: {}", matches)
//
//        matches.forEach { t ->
//            attendeeRepository.insertMany(
//                allUsers.map { user ->
//                    Attendee(
//                        user = user,
//                        eventId = t.id,
//                        availability = Availability.values()[Random.nextInt(Availability.values().size)]
//                    )
//                }
//            )
//        }
//
//        log.info("After attendee additions {}", attendeeRepository.findAll())
//    }
//
//    private fun addEvents(allUsers: Iterable<User>, amountOfEvents: Int) {
//
//    }
}
