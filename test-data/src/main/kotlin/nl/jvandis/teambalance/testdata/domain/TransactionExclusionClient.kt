package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.name
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.Conditional
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.jsonFormatter
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import kotlin.random.Random

private const val TRANSACTION_EXCLUSION_BASE_URL = "/api/transaction-exclusions"

class TransactionExclusionClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val conditional = Conditional(random)
    val aTransactionExclusion = Body.auto<TransactionExclusion>().toLens()

    val aTransactionExclusions = Body.auto<TransactionExclusions>().toLens()

    fun createAndValidateTransactionExclusions(): List<TransactionExclusion> {
        val createdTransactionExclusions =
            (0 until config.amountOfTransactionExclusions).mapNotNull { i ->
                val date =
                    conditional(.1) {
                        java.time.LocalDate
                            .now()
                            .toKotlinLocalDate()
                    }
                val transactionId = conditional(.3) { random.nextInt(5_000, 10_000) }
                val counterParty =
                    conditional(.3) {
                        Arb
                            .name()
                            .take(1)
                            .first()
                            .let { "${it.first} ${it.last}" }
                    }
                val description =
                    if (date == null && transactionId == null && counterParty == null) "Description $i" else null
                val createTransactionExclusion =
                    CreateTransactionExclusion(
                        date = date,
                        transactionId = transactionId,
                        counterParty = counterParty,
                        description = description,
                    )
                try {
                    log.info("Creating transactionExclusion $createTransactionExclusion")
                    val exclusion = createTransactionExclusion(createTransactionExclusion)
                    val exclusion2 = getTransactionExclusion(exclusion.id)

                    if (exclusion != exclusion2) {
                        throw CouldNotCreateEntityException.TransactionExclusionCreationException(
                            "Created transactionExclusion cannot be fetched. " +
                                "It seems something is wrong with the database. " +
                                "Created exclusion: -- $exclusion --, fetched exclusion: -- $exclusion2 --",
                        )
                    }

                    exclusion
                } catch (e: RuntimeException) {
                    throw CouldNotCreateEntityException.TransactionExclusionCreationException(
                        "Could not add transactionExclusion $createTransactionExclusion",
                        e,
                    )
                }
            }

        val allTransactionExclusions = getAllTransactionExclusions()
        if (!allTransactionExclusions.containsAll(createdTransactionExclusions)) {
            throw CouldNotCreateEntityException.TransactionExclusionCreationException(
                "Not all transactionExclusions were created. Created: $createdTransactionExclusions, all: $allTransactionExclusions",
            )
        }
        return createdTransactionExclusions
    }

    private fun getAllTransactionExclusions(): List<TransactionExclusion> {
        val request = Request(Method.GET, TRANSACTION_EXCLUSION_BASE_URL)
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong fetching the transaction-exclusions: ${response.bodyString()}"
        }

        return aTransactionExclusions(response).transactionExclusions
    }

    private fun getTransactionExclusion(id: String): TransactionExclusion {
        val request = Request(Method.GET, "/api/transaction-exclusions/$id")
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong fetching the transaction-exclusion with id $id: ${response.bodyString()}"
        }

        return aTransactionExclusion(response)
    }

    private fun createTransactionExclusion(createTransactionExclusion: CreateTransactionExclusion): TransactionExclusion {
        val request =
            Request(Method.POST, TRANSACTION_EXCLUSION_BASE_URL)
                .body(jsonFormatter.encodeToString(createTransactionExclusion))
        val response: Response = client(request)

        check(response.status.successful) {
            "Something went wrong creating a transactionExclusion: ${response.bodyString()}"
        }

        return aTransactionExclusion(response)
    }

    fun deleteAndValidateTransactionExclusions(transactionExclusion: TransactionExclusion) {
        val request = Request(Method.DELETE, "$TRANSACTION_EXCLUSION_BASE_URL/${transactionExclusion.id}")
        val response = client(request)
        check(response.status.successful) {
            "Something went wrong deleting the transactionExclusion with id ${transactionExclusion.id}: ${response.bodyString()}"
        }

        val remainingTransactionExclusions = getAllTransactionExclusions()
        check(remainingTransactionExclusions.none { it.id == transactionExclusion.id }) {
            "Deleted transactionExclusion with id ${transactionExclusion.id} is still present in the alias pool."
        }
    }
}
