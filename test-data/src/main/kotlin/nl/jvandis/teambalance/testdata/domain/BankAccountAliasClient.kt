package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.usernames
import kotlinx.serialization.encodeToString
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import nl.jvandis.teambalance.testdata.domain.CouldNotCreateEntityException.AliasCreationException
import nl.jvandis.teambalance.testdata.jsonFormatter
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.KotlinxSerialization.auto
import org.slf4j.LoggerFactory
import kotlin.random.Random

class BankAccountAliasClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val aBankAccountAliasLens = Body.auto<BankAccountAlias>().toLens()
    private val aBankAccountAliasesLens = Body.auto<BankAccountAliases>().toLens()

    fun createAndValidateAliases(allUsers: List<User>): List<BankAccountAlias> {
        val bankAccountAliases: List<BankAccountAlias> =
            Arb
                .usernames()
                .take(config.amountOfAliases, RandomSource(random, random.nextLong()))
                .map {
                    CreateBankAccountAlias(it.value, allUsers.random(random).id)
                }.mapNotNull {
                    try {
                        log.info("Creating BankAccountAlias $it")
                        val createdAlias = createAlias(it)
                        val fetchedAlias = getAlias(createdAlias.id)
                        if (createdAlias != fetchedAlias) {
                            throw AliasCreationException(
                                "Created BankAccountAlias cannot be fetched. It seems something is wrong with the database. " +
                                    "Created alias: -- $createdAlias --, fetched alias: -- $fetchedAlias --",
                            )
                        }

                        createdAlias
                    } catch (e: RuntimeException) {
                        throw AliasCreationException(
                            "Could not add bankAccountAlias ${it.alias}",
                            e,
                        )
                    }
                }.toList()

        log.info("All injected aliases: {}", bankAccountAliases)
        val allAliases = getAllAliases()
        if (!allAliases.containsAll(bankAccountAliases)) {
            throw AliasCreationException(
                "Not all bankAccountAliases were created. Created: $bankAccountAliases, all: $allAliases",
            )
        }

        log.info(
            "All aliases: \n{} ",
            allAliases.map { "\n\t Alias '${it.alias}' for user ${it.user.name} (${it.user.id})" },
        )
        return bankAccountAliases
    }

    fun deleteAndValidateAlias(alias: BankAccountAlias) {
        val request = Request(Method.DELETE, "/api/aliases/${alias.id}")
        val response = client(request)
        check(response.status.successful) {
            "Something went wrong deleting the alias with id ${alias.id}: ${response.bodyString()}"
        }

        val remainingAliases = getAllAliases()
        check(remainingAliases.none { it.id == alias.id }) {
            "Deleted alias with id ${alias.id} is still present in the alias pool."
        }
    }

    private fun createAlias(alias: CreateBankAccountAlias): BankAccountAlias {
        log.info("Creating bank account alias: $alias")
        val request =
            Request(Method.POST, "/api/aliases")
                .body(jsonFormatter.encodeToString(alias))
        val response = client(request)

        if (!response.status.successful) {
            throw AliasCreationException(
                "Failed to create bank account alias: $alias. Status: ${response.status}",
            )
        }

        return aBankAccountAliasLens.extract(response)
    }

    private fun getAlias(id: String): BankAccountAlias {
        val request = Request(Method.GET, "/api/aliases/$id")
        val response = client(request)

        if (!response.status.successful) {
            throw AliasCreationException(
                "Failed to get bank account alias with ID: $id. Status: ${response.status}",
            )
        }

        return aBankAccountAliasLens.extract(response)
    }

    fun getAllAliases(): List<BankAccountAlias> {
        val request = Request(Method.GET, "/api/aliases")
        val response = client(request)

        if (!response.status.successful) {
            throw AliasCreationException("Failed to get all bank account aliases. Status: ${response.status}")
        }

        return aBankAccountAliasesLens.extract(response).bankAccountAliases
    }
}
