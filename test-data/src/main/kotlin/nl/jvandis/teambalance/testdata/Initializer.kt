package nl.jvandis.teambalance.testdata

import kotlinx.serialization.json.Json
import nl.jvandis.teambalance.testdata.domain.AttendeeClient
import nl.jvandis.teambalance.testdata.domain.BankAccountAlias
import nl.jvandis.teambalance.testdata.domain.BankAccountAliasClient
import nl.jvandis.teambalance.testdata.domain.Event
import nl.jvandis.teambalance.testdata.domain.EventClient
import nl.jvandis.teambalance.testdata.domain.MatchClient
import nl.jvandis.teambalance.testdata.domain.MiscEvent
import nl.jvandis.teambalance.testdata.domain.TrainingClient
import nl.jvandis.teambalance.testdata.domain.TransactionExclusionClient
import nl.jvandis.teambalance.testdata.domain.User
import nl.jvandis.teambalance.testdata.domain.UserClient
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.cookie.cookie
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.time.measureTime

val jsonFormatter =
    Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
//    prettyPrint = true
    }

class Initializer(
    apiKey: String,
    username: String,
    password: String,
    random: Random,
    private val host: String = "http://localhost:8080",
    private val config: SpawnDataConfig,
) {
    private var sessionId: String? = null
    private val log = LoggerFactory.getLogger(javaClass)
    private val client: HttpHandler = addHeaders(apiKey, host)(ApacheClient())

    private val userClient = UserClient(client, random, config)
    private val bankAccountAliasClient = BankAccountAliasClient(client, random, config)
    private val transactionExclusionClient = TransactionExclusionClient(client, random, config)
    private val attendeeClient = AttendeeClient(client, random, config)
    private val trainingClient = TrainingClient(client, random, config, attendeeClient)
    private val matchClient = MatchClient(client, random, config, attendeeClient)
    private val eventClient = EventClient(client, random, config, attendeeClient)

    val aMiscEvent = Body.auto<Event<MiscEvent>>().toLens()

    init {
        val loginResponse =
            runCatching {
                client(
                    Request(POST, "$host/login")
                        .header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                        ).header("Content-Type", "application/x-www-form-urlencoded")
                        .body("username=$username&password=$password"),
                )
            }

        loginResponse
            .onSuccess { response ->
                check(response.status == org.http4k.core.Status.FOUND && response.header("Location") == "/") {
                    "Login failed, expected 302 with Location '/' but got ${response.status} and location ${
                        response.header(
                            "Location",
                        )
                    }"
                }
                log.info("Login successful")
                val header = response.header("Set-Cookie")
                println("Header: $header")
                sessionId = header?.substringBefore(";")?.substringAfter("JSESSIONID=")
                println("Session id: $sessionId")
            }.onFailure { exception ->
                log.error("Login failed with exception", exception)
            }
    }

    private fun addHeaders(
        apiKey: String,
        baseUri: String,
    ) = Filter { next ->
        {
            val header =
                it
                    .run { if (sessionId != null) cookie("JSESSIONID", sessionId!!) else this }
                    .header("X-Secret", apiKey)
                    .header("Content-Type", ContentType.APPLICATION_JSON.value)
            val apply =
                header
                    .run {
                        if (it.uri.host.isBlank()) {
                            return@run uri(Uri.of(baseUri + it.uri.path + "?" + it.uri.query))
                        } else {
                            return@run this
                        }
                    }
            apply
                .let(next)
        }
    }

    fun spawnData() {
        measureTime { initializeUsers() }.also { log.info("Created ${config.amountOfUsers} users in $it") }

        log.info("All users in the system: ")
        val allUsers = userClient.getAllUsers()
        log.info("All users (${allUsers.size}): {}", allUsers.map { "\n\t $it" })

        measureTime { createAndValidateTrainings(allUsers) }.also { log.info("Created ${config.amountOfTrainings} trainings in $it") }
        measureTime { addMatches(allUsers) }.also { log.info("Created ${config.amountOfMatches} matches in $it") }
        measureTime { addEvents(allUsers) }.also { log.info("Created ${config.amountOfEvents} misc events in $it") }
        measureTime { createAndValidateAliases(allUsers) }.also { log.info("Created ${config.amountOfAliases} aliases in $it") }
        measureTime { createTransactionExclusions() }
            .also { log.info("Created ${config.amountOfTransactionExclusions} transaction exclusions in $it") }
    }

    private fun createAndValidateAliases(allUsers: List<User>) {
        val createdBankAccountAliases: List<BankAccountAlias> =
            bankAccountAliasClient.createAndValidateAliases(allUsers)

        // Ensure aliases can be deleted
        bankAccountAliasClient.deleteAndValidateAlias(createdBankAccountAliases.last())
    }

    private fun initializeUsers() {
        val createdUsers: List<User> = userClient.createAndValidateUsers()

        // Ensure users can be updated
        userClient.updateAndValidateUser(
            createdUsers.last(),
        )

        // Ensure users can be deleted
        userClient.deleteAndValidateUser(createdUsers.last())
    }

    private fun createTransactionExclusions() {
        val createdTransactionExclusions =
            transactionExclusionClient.createAndValidateTransactionExclusions()

        // Delete mapping
        transactionExclusionClient.deleteAndValidateTransactionExclusions(createdTransactionExclusions.last())
    }

    private fun createAndValidateTrainings(allUsers: List<User>) {
        val trainings = trainingClient.createAndValidateTrainings(allUsers)

        // Ensure trainings can be updated
        trainingClient.updateAndValidateTraining(trainings.last())

        // Ensure trainings can be deleted
        trainingClient.deleteTraining(trainings.last().id)
    }

    private fun addMatches(allUsers: List<User>) {
        matchClient.addMatches(allUsers)
    }

    private fun addEvents(allUsers: List<User>) {
        eventClient.addEvents(allUsers)
    }
}

data class SpawnDataConfig(
    val amountOfUsers: Int,
    val amountOfTrainings: Int,
    val amountOfMatches: Int,
    val amountOfEvents: Int,
    val amountOfAliases: Int,
    val amountOfTransactionExclusions: Int,
)
