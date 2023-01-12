package nl.jvandis.teambalance.api.attendees

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AttendeeRepository : CrudRepository<Attendee, Long> {

    @Query("SELECT a,u,e from Attendee a LEFT JOIN Uzer u on a.user.id = u.id LEFT JOIN Event e on a.event.id = e.id where a.event.id in :eventIds")
    fun findAllByEventIdIn(eventIds: List<Long>, sort: Sort = Sort.by("user.role", "user.name")): List<Attendee>

    @Query("SELECT a,u,e from Attendee a LEFT JOIN Uzer u on a.user.id = u.id LEFT JOIN Event e on a.event.id = e.id where u.id in :userIds")
    fun findAllByUserIdIn(userIds: List<Long>): List<Attendee>

    @Query("SELECT a,u,e from Attendee a LEFT JOIN Uzer u on a.user.id = u.id LEFT JOIN Event e on a.event.id = e.id where a.event.id in :eventIds and u.id in :userIds ")
    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>): List<Attendee>

    fun findByUserIdAndEventId(userId: Long, eventId: Long): List<Attendee>
}
