package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.games.cluedoLocations
import io.kotest.property.arbs.stockExchanges
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.EventCreationException
import nl.jvandis.teambalance.testdata.jsonFormatter
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import kotlin.random.Random

private const val TRAINING_BASE_URL = "/api/trainings"

class TrainingClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
    private val attendeeClient: AttendeeClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val aTrainingsLens = Body.auto<Event<Training>>().toLens()
    private val aTrainingLens = Body.auto<Training>().toLens()

    fun deleteTraining(id: String) {
        val request =
            Request(Method.DELETE, "$TRAINING_BASE_URL/$id")
                .query("delete-attendees", "true")
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong deleting the training with id $id: [${response.status}] ${response.bodyString()}"
        }

        log.info("Successfully deleted the training with id $id")

        val remainingTrainings = getAllTrainings()
        check(remainingTrainings.none { it.id == id }) {
            "Deleted training with id $id is still present."
        }
    }

    fun getAllTrainings(): List<Training> {
        val request =
            Request(Method.GET, TRAINING_BASE_URL)
                .query("limit", "1000")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went wrong getting all trainings: [${response.status}] ${response.bodyString()}"
        }

        return aTrainingsLens.extract(response).events
    }

    private fun createTraining(training: CreateTraining): Event<Training> {
        val request = Request(POST, TRAINING_BASE_URL).body(jsonFormatter.encodeToString(training))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a training: [${response.status}] ${response.bodyString()}"
        }

        return aTrainingsLens(response)
    }

    private fun addTrainer(
        id: String,
        trainerId: String?,
    ): Training {
        val request =
            Request(PUT, "/api/trainings/$id/trainer").body(
                trainerId?.let { """{ "userId": "$it"  }""" } ?: "",
            )

        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong adding a trainer: [${response.status}] ${response.bodyString()}"
        }

        return aTrainingLens(response)
    }

    fun createAndValidateTrainings(allUsers: List<User>): List<Training> {
        val locations =
            Arb
                .cluedoLocations()
                .take(10, RandomSource(random, random.nextLong()))
                .map { it.name }
                .toList()
        val comments =
            Arb
                .stockExchanges()
                .take(2, RandomSource(random, random.nextLong()))
                .map { it.name }
                .toList() + null

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
        val createdTrainings =
            (0 until config.amountOfTrainings).map { i ->
                try {
                    log.info("Creating Training ${i + 1}")
                    val training =
                        CreateTraining(
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
                            comment = comments.random(),
                        )

                    val savedTraining = createTraining(training).events.first()
                    log.info("Added training with id ${savedTraining.id}: $savedTraining")

                    val addedAttendees = attendeeClient.createAndValidateAttendees(allUsers, savedTraining.id)
                    log.info("Added attendees to training with id ${savedTraining.id}: ${addedAttendees.map { it.user.name }}")

                    // Add trainer, 50% chance
                    if (random.nextBoolean() && addedAttendees.isNotEmpty()) {
                        val trainerId = addedAttendees.take(1).map { it.user.id }.firstOrNull()
                        val updatedTraining = addTrainer(savedTraining.id, trainerId)

                        log.info("Added trainer to training with id ${savedTraining.id}: ${updatedTraining.trainer}")
                    } else {
                        log.info("No trainer will be added to training with id ${savedTraining.id}")
                    }
                    savedTraining
                } catch (e: Exception) {
                    throw EventCreationException("Could not add training $i", e)
                }
            }

        val allTrainings = getAllTrainings()
        if (!allTrainings.map { it.id }.containsAll(createdTrainings.map { it.id })) {
            throw EventCreationException(
                "Not all trainings were created." +
                    "\nCreated: ${createdTrainings.map { "\n\t ${it.id}" }}, " +
                    "\nAll: ${allTrainings.map { "\n\t ${it.id}" }}" +
                    "\n Diff: ${createdTrainings.map{it.id}.minus(allTrainings.map{it.id}.toSet()).map { "\n\t $it" }}",
            )
        }
        log.info("Done adding ${config.amountOfTrainings} trainings")

        return createdTrainings
    }

    fun updateAndValidateTraining(training: Training) {
        log.warn("NOT IMPLEMENTED: updateAndValidateTraining")
    }
}
