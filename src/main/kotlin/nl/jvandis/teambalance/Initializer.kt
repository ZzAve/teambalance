package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.training.TrainingRepository
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.ZoneOffset

@Configuration
class Initializer(
        private val userRepository: UserRepository,
        private val trainingRepository: TrainingRepository,
        private val attendeeRepository: AttendeeRepository

) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Bean
    fun sendDatabase(): InitializingBean? {
        return InitializingBean {
            userRepository.saveAll(listOf(
                    User("Julius", Role.DIAGONAL),
                    User("Maurice", Role.COACH),
                    User("Bocaj", Role.MID),
                    User("Joep", Role.PASSER))
            )

            log.info("After user injection")
            val users = userRepository.findAll()
            log.info("ALl users: ", users)
            trainingRepository.save(Training(startTime = LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC), location = "Training plaza", comment = ""))
            trainingRepository.save(Training(startTime = LocalDateTime.now().minusDays(10).toInstant(ZoneOffset.UTC), location = "adsfadf,asdf", comment = ""))
            trainingRepository.save(Training(startTime = LocalDateTime.now().minusDays(20).toInstant(ZoneOffset.UTC), location = "Training,asdf", comment = ""))
            trainingRepository.save(Training(startTime = LocalDateTime.now().minusDays(22).toInstant(ZoneOffset.UTC), location = "Train,asdf", comment = ""))

            log.info("After training injection")
            val trainings = trainingRepository.findAll()
            log.info("ALl trainings: ", trainings)


            trainings.forEach { t ->
                attendeeRepository.saveAll(users.map { user ->
                    Attendee(user, t)
                })
            }

            log.info("After attendee additions", attendeeRepository.findAll())
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
