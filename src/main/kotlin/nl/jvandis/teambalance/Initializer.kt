package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.attendees.Attendee
import nl.jvandis.teambalance.api.attendees.AttendeeRepository
import nl.jvandis.teambalance.api.attendees.Availability
import nl.jvandis.teambalance.api.bank.BankAccountAlias
import nl.jvandis.teambalance.api.bank.BankAccountAliasRepository
import nl.jvandis.teambalance.api.bank.BankAccountTransactionExclusionRepository
import nl.jvandis.teambalance.api.bank.TransactionExclusion
import nl.jvandis.teambalance.api.event.EventRepository
import nl.jvandis.teambalance.api.match.Match
import nl.jvandis.teambalance.api.training.Training
import nl.jvandis.teambalance.api.users.Role
import nl.jvandis.teambalance.api.users.User
import nl.jvandis.teambalance.api.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import kotlin.random.Random

@Configuration
@Profile("dev") // don't use class unless 'dev' profile is activated
class Initializer(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val attendeeRepository: AttendeeRepository,
    private val bankAccountAliasRepository: BankAccountAliasRepository,
    private val bankAccountTransactionExclusionRepository: BankAccountTransactionExclusionRepository

) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun initializeDatabase(): InitializingBean? {
        return InitializingBean {
            userRepository.saveAll(
                listOf(
                    User("Julius", Role.DIAGONAL),
                    User("Maurice", Role.COACH),
                    User("Bocaj", Role.MID),
                    User("Joep", Role.PASSER),
                    User("Roger", Role.PASSER),
                    User("Pardoes", Role.COACH)
                )
            )

            log.info("After user injection")
            val users = userRepository.findAll()
            log.info("All users: ", users)
            addTrainings(users)
            addMatches(users)

            bankAccountAliasRepository.saveAll(
                listOf(
                    BankAccountAlias("J. van Dis", users.first { it.name == "Julius" }),
                    BankAccountAlias("J. Post", users.first { it.name == "Bocaj" }),
                    BankAccountAlias("Hr E. Fens", users.first { it.name == "Maurice" }),
                    BankAccountAlias("M.A. Haga", users.first { it.name == "Joep" })
                )
            )

            log.info("After alias injection")
            val aliases = bankAccountAliasRepository.findAll()
            log.info("All aliases: {}", aliases)

            bankAccountTransactionExclusionRepository.saveAll(
                listOf(
                    TransactionExclusion(counterParty = "CCV*BUITEN IN DE KUIL")
                )
            )
        }
    }

    private fun addTrainings(users: Iterable<User>) {
        eventRepository.save(
            Training(
                startTime = LocalDateTime.now().minusDays(3),
                location = "Training plaza",
                comment = "No, this is patrick"
            )
        )
        eventRepository.save(
            Training(
                startTime = LocalDateTime.now().plusDays(10),
                location = "adsfadf,asdf",
                comment = "",
                trainer = users.first()
            )
        )
        eventRepository.save(
            Training(
                startTime = LocalDateTime.now().minusDays(20),
                location = "Training,asdf",
                comment = ""
            )
        )
        eventRepository.save(
            Training(
                startTime = LocalDateTime.now().plusDays(22),
                location = "Train,asdf",
                comment = ""
            )
        )

        log.info("After training injection")
        val trainings = eventRepository.findAll().filterIsInstance<Training>()
        log.info("ALl trainings: {} ", trainings)

        trainings.forEach { t ->
            attendeeRepository.saveAll(
                users.map { user ->
                    Attendee(
                        user,
                        t,
                        availability = Availability.values()[Random.nextInt(Availability.values().size)]
                    )
                }
            )
        }

        log.info("After attendee additions", attendeeRepository.findAll())
    }

    private fun addMatches(users: Iterable<User>) {
        eventRepository.save(
            Match(
                startTime = LocalDateTime.now().minusDays(3),
                location = "Match plaza",
                comment = "No, this is patrick"
            )
        )
        eventRepository.save(
            Match(
                startTime = LocalDateTime.now().plusDays(10),
                location = "123123,asdf",
                comment = ""
            )
        )
        eventRepository.save(
            Match(
                startTime = LocalDateTime.now().minusDays(20),
                location = "Match,asdf",
                comment = ""
            )
        )
        eventRepository.save(
            Match(
                startTime = LocalDateTime.now().plusDays(22),
                location = "Match,asdf",
                comment = ""
            )
        )

        log.info("After Match injection")
        val matches = eventRepository.findAll().filterIsInstance<Match>()
        log.info("ALl Match: ", matches)

        matches.forEach { t ->
            attendeeRepository.saveAll(
                users.map { user ->
                    Attendee(
                        user,
                        t,
                        availability = Availability.values()[Random.nextInt(Availability.values().size)]
                    )
                }
            )
        }

        log.info("After attendee additions", attendeeRepository.findAll())
    }
}
