package nl.jvandis.teambalance.api.users

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "users")
@RequestMapping(path = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getUsers(
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
    ): ExternalUsers {
        log.debug("getUsers")

        return Users(
            users =
                userRepository.findAll(Sort.by("name"))
                    .filter { includeInactiveUsers || it.isActive }
                    .filterNotNull(),
        ).expose()
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable(value = "id") userId: Long,
    ): ExternalUser {
        log.debug("getUser $userId")

        return userRepository.findByIdOrNull(userId)?.expose() ?: throw InvalidUserException(userId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(
        @RequestBody potentialUser: PotentialUser,
    ) {
        log.debug("postUser $potentialUser")

        val user = potentialUser.internalize()
        userRepository.insert(user)
    }

    @Admin
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") userId: Long,
        @RequestBody potentialUserUpdate: PotentialUserUpdate,
    ): ExternalUser {
        log.debug("updatingUser: $potentialUserUpdate")

        return userRepository
            .findByIdOrNull(userId)
            ?.updateUser(potentialUserUpdate, userId)
            ?.expose()
            ?: throw InvalidUserException(userId)
    }

    private fun User.updateUser(
        potentialUserUpdate: PotentialUserUpdate,
        userId: Long,
    ): User {
        val updatedUser =
            copy(
                name = potentialUserUpdate.name ?: name,
                role = potentialUserUpdate.role ?: role,
                isActive = potentialUserUpdate.isActive ?: isActive,
                jerseyNumber = potentialUserUpdate.jerseyNumber ?: jerseyNumber,
            )

        return try {
            userRepository.update(updatedUser)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Could not update user $userId to $potentialUserUpdate, name already in use")
        }
    }

    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") userId: Long,
    ) {
        log.debug("deletingUser: $userId")

        try {
            userRepository.deleteById(userId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("User $userId could not be deleted. User is still bound to trainings")
        }
    }
}

data class PotentialUser(
    val name: String,
    val role: Role,
) {
    fun internalize() = User(name, role)

    fun internalize(id: Long) = User(id = id, name = name, role = role)
}

data class PotentialUserUpdate(
    val name: String?,
    val role: Role?,
    val isActive: Boolean?,
    val jerseyNumber: Int?,
)
