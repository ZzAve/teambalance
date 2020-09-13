package nl.jvandis.teambalance.api.event

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
interface EventRepository : CrudRepository<Event, Long>
