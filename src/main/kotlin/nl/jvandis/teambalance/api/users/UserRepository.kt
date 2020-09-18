package nl.jvandis.teambalance.api.users

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository

@Repository
interface UserRepository : PageableRepository<User, Long>
