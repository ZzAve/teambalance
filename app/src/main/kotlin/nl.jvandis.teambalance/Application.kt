package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.bank.BankService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@ConfigurationPropertiesScan
class Application

fun main(args: Array<String>) {
    val applicationContext = SpringApplication.run(Application::class.java, *args)

    warmUp(applicationContext)
}

private fun warmUp(applicationContext: ConfigurableApplicationContext) {
    val log = loggerFor("nl.jvandis.teambalance.TeamBalanceApplication")

    Tenant.values().forEach {
        log.info("Retrieving balance and transactions for $it")
        MultiTenantContext.setCurrentTenant(it)
        applicationContext.getBean(BankService::class.java).getBalance()
        applicationContext.getBean(BankService::class.java).getTransactions(1, 0)
        MultiTenantContext.clear()
    }

    log.info("Retrieved balance and transactions to warm up cache")
}
