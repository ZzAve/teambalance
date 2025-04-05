package nl.jvandis.teambalance.testdata.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.firstName
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
import java.time.Instant
import kotlin.random.Random

class UserClient(
    private val client: HttpHandler,
    private val random: Random,
    private val config: SpawnDataConfig,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    val aUserLens = Body.auto<User>().toLens()
    val aUsersLens = Body.auto<Users>().toLens()

    private fun createUser(user: CreateUser): User {
        val body = jsonFormatter.encodeToString(user)
        val request = Request(Method.POST, "/api/users").body(body)
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went wrong creating a user: ${response.bodyString()}"
        }

        return aUserLens(response)
    }

    private fun getUser(id: String): User {
        val request = Request(Method.GET, "/api/users/$id")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went fetching use with id $id: ${response.bodyString()}"
        }

        return aUserLens(response)
    }

    fun getAllUsers(): List<User> {
        val request = Request(Method.GET, "/api/users")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went fetching users: ${response.bodyString()}"
        }

        return aUsersLens(response).users
    }

    fun deleteAndValidateUser(user: User) {
        val request = Request(Method.DELETE, "/api/users/${user.id}")
        val response: Response = client(request)
        check(response.status.successful) {
            "Something went wrong deleting the user with id ${user.id}: ${response.bodyString()}"
        }

        val remainingUsers = getAllUsers()
        check(remainingUsers.none { it.id == user.id }) {
            "Deleted user with id ${user.id} is still present in the user pool."
        }
    }

    fun updateAndValidateUser(user: User): User {
        val now = Instant.now().toEpochMilli()
        val jerseyNumber = random.nextInt(10, 100)
        val role = Role.entries.toTypedArray().random(random)
        val potentialUserUpdate = PotentialUserUpdate(name = "Updated $now ${user.name}", role, null, jerseyNumber)

        val request = Request(Method.PUT, "/api/users/${user.id}")
        val body = jsonFormatter.encodeToString(potentialUserUpdate)
        val response: Response = client(request.body(body))

        check(response.status.successful) {
            "Something went wrong updating the user with id $user:  ${response.status}: ${response.bodyString()}"
        }

        val updatedUser = aUserLens(response)
        val updatedUser2 = getUser(user.id)
        check(
            updatedUser == updatedUser2 &&
                updatedUser.name == "Updated $now ${user.name}" &&
                updatedUser.role == role,
        ) {
            "The update for user with id ${updatedUser.id} was not stored correctly."
        }

        return updatedUser
    }

    fun createAndValidateUsers(): List<User> {
        val users = mutableListOf<User>()
        val usersToCreate =
            Arb
                .firstName()
                .take(config.amountOfUsers)
                .map { it.name }
                .map {
                    CreateUser(it, Role.entries.random(random))
                }.toList()

        val createdUsers: List<User> =
            usersToCreate.mapNotNull {
                log.info("Creating user ${it.name} with role ${it.role}")
                val result: Result<User> =
                    kotlin.runCatching {
                        createUser(it)
                    }

                if (result.isSuccess) {
                    val user = result.getOrNull() ?: error("Shouldn't be null")
                    log.info("Created user ${user.name} with role ${user.role}. Id ${user.id}")
                    users.add(user)

                    val fetchedUser = getUser(user.id)
                    if (user != fetchedUser) {
                        throw CouldNotCreateEntityException.UserCreationException(
                            "Created user cannot be fetched. It seems something is wrong with the database. " +
                                "Created user: -- $user --, fetched user: -- $fetchedUser --",
                        )
                    }

                    user
                } else {
                    log.error(
                        "Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}",
                        result.exceptionOrNull(),
                    )
                    throw CouldNotCreateEntityException.UserCreationException(
                        "Could not create user ${it.name} [${it.role}]: ${result.exceptionOrNull()?.message}",
                        result.exceptionOrNull(),
                    )
                    null
                }
            }

        // Ensure all users are present
        val allUsers = getAllUsers()
        if (!allUsers.containsAll(createdUsers)) {
            val missingUsers = createdUsers.filter { !allUsers.contains(it) }
            throw CouldNotCreateEntityException.UserCreationException(
                "Not all users were created exist in the user pool. Created: $createdUsers, missing users: $missingUsers",
            )
        }

        return createdUsers
    }
}
