package nl.jvandis.teambalance

import io.micronaut.runtime.Micronaut
import nl.jvandis.teambalance.api.bank.BankService
import org.slf4j.LoggerFactory.getLogger


fun main(args: Array<String>) {
    val applicationContext = Micronaut.build()
        .args(*args)
        .packages("nl.jvandis.teambalance")
        .start()

    applicationContext.getBean(BankService::class.java).getBalance()
    applicationContext.getBean(BankService::class.java).getTransactions(1, 0)
    log.info("Retrieved balance and transactions to warm up cache")
}


private val log = getLogger("TeamBalanceApplication")
