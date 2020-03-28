package nl.jvandis.teambalance.api.users

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.DataConstraintViolationException
import nl.jvandis.teambalance.api.InvalidUserException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Api(tags = ["users"])
@RequestMapping(path = ["/api/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
        private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @GetMapping
    fun getUsers(): Users {
        log.info("getUsers")
        return Users(users = userRepository.findAll().filterNotNull())
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable(value="id") userId: Long): User{
        log.info("getUser $userId")
        return userRepository.findByIdOrNull(userId) ?: throw InvalidUserException(userId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun postUser(@RequestBody potentialUser: PotentialUser) {
        log.info("postUser $potentialUser")
        val user = potentialUser.internalize()
        userRepository.save(user)
    }

    @PutMapping("/{id}")
    fun updateUser(
            @PathVariable(value = "id") userId: Long,
            @RequestBody potentialUserUpdate: PotentialUserUpdate
    ): User {
        log.info("updatingUser: $potentialUserUpdate")
        return userRepository
                .findByIdOrNull(userId)
                ?.let {
                    val x = it.copy(
                            name = potentialUserUpdate.name ?: it.name,
                            role = potentialUserUpdate.role ?: it.role
                    )

                    try {
                        userRepository.save(x)
                    } catch (e: DataIntegrityViolationException){
                        throw DataConstraintViolationException("Could not update user $userId to $potentialUserUpdate, name already in use")
                    }
                } ?: throw InvalidUserException(userId)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    fun updateUser(
            @PathVariable(value = "id") userId: Long
    ) {
        log.info("deletingUser: $userId")
        try {
            userRepository.deleteById(userId)
        } catch (e: DataIntegrityViolationException){
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
        val role: Role?
) {

//    fun internalize() = User(name, role)

//    fun internalize(id: Long) =  User(id = id, name = name, role = role)
}