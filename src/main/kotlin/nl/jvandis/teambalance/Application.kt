package nl.jvandis.teambalance

import io.micronaut.runtime.Micronaut
import nl.jvandis.teambalance.api.bank.BankService
import org.slf4j.LoggerFactory.getLogger
// import org.springframework.boot.SpringApplication
// import org.springframework.boot.autoconfigure.SpringBootApplication
// import org.springframework.boot.context.properties.ConfigurationPropertiesScan
// import org.springframework.cache.annotation.EnableCaching




fun main(args: Array<String>) {
    val applicationContext = Micronaut.build()
        .args(*args)
        .packages("nl.jvandis.teambalance.*")
        .start()

    applicationContext.getBean(BankService::class.java).getBalance()
    applicationContext.getBean(BankService::class.java).getTransactions(1, 0)
    log.info("Retrieved balance and transactions to warm up cache")
}


// @SpringBootApplication
// @EnableCaching
// @ConfigurationPropertiesScan
// class Application

private val log = getLogger("TeamBalanceApplication")
//
// fun main(args: Array<String>) {
//     val applicationContext = SpringApplication.run(Application::class.java, *args)
//
//     applicationContext.getBean(BankService::class.java).getBalance()
//     applicationContext.getBean(BankService::class.java).getTransactions(1, 0)
//     log.info("Retrieved balance and transactions to warm up cache")
// }
