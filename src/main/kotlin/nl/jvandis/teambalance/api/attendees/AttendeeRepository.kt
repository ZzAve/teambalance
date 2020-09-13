package nl.jvandis.teambalance.api.attendees

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.Sort
import io.micronaut.data.repository.CrudRepository

interface AttendeeRepository : CrudRepository<Attendee, Long> {

    @Query("SELECT a,u,e from Attendee a LEFT JOIN Uzer u on a.user.id = u.id LEFT JOIN Event e on a.event.id = e.id where a.event.id in :eventIds")
    fun findAllByEventIdIn(eventIds: List<Long>, sort: Sort = Sort.of(Sort.Order("user.role"), Sort.Order("user.name"))): List<Attendee>
    fun findAllByUserIdIn(userIds: List<Long>): List<Attendee>
    fun findALlByEventIdInAndUserIdIn(eventIds: List<Long>, userIds: List<Long>): List<Attendee>
    fun findByUserIdAndEventId(userId: Long, trainingId: Long): List<Attendee>
}
