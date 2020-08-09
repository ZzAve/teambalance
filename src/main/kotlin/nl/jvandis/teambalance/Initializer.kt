package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.Availability
import nl.jvandis.teambalance.api.training.EventRepository
import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import java.time.LocalDateTime
import kotlin.random.Random

// @Configuration
class Initializer(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val attendeeRepository: AttendeeRepository

) {

    private val log = LoggerFactory.getLogger(javaClass)
    // @Bean
    fun sendDatabase(): InitializingBean? {

        return InitializingBean {
            if (false) {
                userRepository.saveAll(
                    listOf(
                        User("Julius", Role.DIAGONAL),
                        User("Maurice", Role.COACH),
                        User("Bocaj", Role.MID),
                        User("Joep", Role.PASSER)
                    )
                )

                log.info("After user injection")
                val users = userRepository.findAll()
                log.info("ALl users: ", users)
                eventRepository.save(Training(startTime = LocalDateTime.now().minusDays(3), location = "Training plaza", comment = "No, this is patrick"))
                eventRepository.save(Training(startTime = LocalDateTime.now().plusDays(10), location = "adsfadf,asdf", comment = ""))
                eventRepository.save(Training(startTime = LocalDateTime.now().minusDays(20), location = "Training,asdf", comment = ""))
                eventRepository.save(Training(startTime = LocalDateTime.now().plusDays(22), location = "Train,asdf", comment = ""))

                log.info("After training injection")
                val trainings = eventRepository.findAll()
                log.info("ALl trainings: ", trainings)

                trainings.forEach { t ->
                    attendeeRepository.saveAll(
                        users.map { user ->
                            Attendee(user, t, availability = Availability.values()[Random.nextInt(Availability.values().size)])
                        }
                    )
                }

                log.info("After attendee additions", attendeeRepository.findAll())
            }
        }
    }
}
//    @Bean
//    fun initializeTraining(): InitializingBean{
//        return () -> {
//            userRepository.save(User("Julius", Role.DIAGONAL))
//            userRepository.save(User("Julius", Role.DIAGONAL))
//        }
//    }
