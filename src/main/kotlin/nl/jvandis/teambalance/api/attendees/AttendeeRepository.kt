package nl.jvandis.teambalance.api.attendees

import org.springframework.data.repository.CrudRepository

interface AttendeeRepository : CrudRepository<Attendee, Long>{
    fun findAllByTrainingIdIn(trainingIds: List<Long>) : List<Attendee>
    fun findAllByUserIdIn(userIds: List<Long>) : List<Attendee>
    fun findALlByTrainingIdInAndUserIdIn(trainingIds: List<Long>, userIds: List<Long>) : List<Attendee>
}


