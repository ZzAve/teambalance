package nl.jvandis.teambalance.api.attendees

import org.springframework.data.domain.Sort
import org.springframework.data.repository.CrudRepository

interface AttendeeRepository : CrudRepository<Attendee, Long> {
    fun findAllByEventIdIn(eventIds: List<Long>, sort: Sort = Sort.by("userName")): List<Attendee>
    fun findAllByUserIdIn(userIds: List<Long>): List<Attendee>
    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>): List<Attendee>
    fun findByUserIdAndEventId(userId: Long, trainingId: Long): List<Attendee>
}
