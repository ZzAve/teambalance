package nl.jvandis.teambalance.api.training

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/api/users"])
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
            @RequestBody potentialUser: PotentialUser
    ) {
        log.info("updatingUser: $potentialUser")
        userRepository.save(potentialUser.internalize(userId))
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

    fun internalize(id: Long) =  User(id = id, name = name, role = role)
}