package nl.jvandis.teambalance.api.users

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService
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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["users"])
@RequestMapping(path = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val userRepository: UserRepository,
    private val secretService: SecretService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getUsers(
        @RequestParam(value = "include-inactive-users", defaultValue = "false") includeInactiveUsers: Boolean,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?,
    ): Users {
        log.debug("getUsers")
        secretService.ensureSecret(secret)

        return Users(
            users = userRepository.findAll(Sort.by("name"))
                .filter { includeInactiveUsers || it.isActive }
                .filterNotNull()
        )
    }

    @GetMapping("/{id}")
    fun getUser(
        @PathVariable(value = "id") userId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ): User {
        log.debug("getUser $userId")
        secretService.ensureSecret(secret)

        return userRepository.findByIdOrNull(userId) ?: throw InvalidUserException(userId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(
        @RequestBody potentialUser: PotentialUser,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("postUser $potentialUser")
        secretService.ensureSecret(secret)

        val user = potentialUser.internalize()
        userRepository.save(user)
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") userId: Long,
        @RequestBody potentialUserUpdate: PotentialUserUpdate,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?

    ): User {
        log.debug("updatingUser: $potentialUserUpdate")
        secretService.ensureSecret(secret)

        return userRepository
            .findByIdOrNull(userId)
            ?.updateUser(potentialUserUpdate, userId)
            ?: throw InvalidUserException(userId)
    }

    private fun User.updateUser(
        potentialUserUpdate: PotentialUserUpdate,
        userId: Long
    ): User {
        val updatedUser = copy(
            name = potentialUserUpdate.name ?: name,
            role = potentialUserUpdate.role ?: role,
            isActive = potentialUserUpdate.isActive ?: isActive
        )

        return try {
            userRepository.save(updatedUser)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("Could not update user $userId to $potentialUserUpdate, name already in use")
        }
    }

    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun updateUser(
        @PathVariable(value = "id") userId: Long,
        @RequestHeader(value = SECRET_HEADER, required = false) secret: String?
    ) {
        log.debug("deletingUser: $userId")
        secretService.ensureSecret(secret)

        try {
            userRepository.deleteById(userId)
        } catch (e: DataIntegrityViolationException) {
            throw DataConstraintViolationException("User $userId could not be deleted. User is still bound to trainings")
        }
    }
}

data class PotentialUser(
    val name: String,
    val role: Role
) {

    fun internalize() = User(name, role)

    fun internalize(id: Long) = User(id = id, name = name, role = role)
}

data class PotentialUserUpdate(
    val name: String?,
    val role: Role?,
    val isActive: Boolean?
)
