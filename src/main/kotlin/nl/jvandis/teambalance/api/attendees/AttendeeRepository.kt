package nl.jvandis.teambalance.api.attendees

import org.springframework.data.repository.CrudRepository

interface AttendeeRepository : CrudRepository<Attendee, Long>{
    fun findAllByEventIdIn(eventIds: List<Long>) : List<Attendee>
    fun findAllByUserIdIn(userIds: List<Long>) : List<Attendee>
    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>) : List<Attendee>
}
