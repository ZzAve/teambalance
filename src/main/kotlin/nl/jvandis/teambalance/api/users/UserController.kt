package nl.jvandis.teambalance.api.users

import io.swagger.annotations.Api
import nl.jvandis.teambalance.api.InvalidUserException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
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

                    userRepository.save(x)

                } ?: throw InvalidUserException(userId)
    }


    @DeleteMapping("/{id}")
    fun updateUser(
            @PathVariable(value = "id") userId: Long
    ) {
        log.info("deletingUser: $userId")
        userRepository.deleteById(userId);
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