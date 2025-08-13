package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.bank.BankService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

internal inline val <reified T> T.log: Logger
    get() = LoggerFactory.getLogger(T::class.java)

private val logger = LoggerFactory.getLogger("nl.jvandis.teambalance.init")

fun main(args: Array<String>) {
    val applicationContext = SpringApplication.run(Application::class.java, *args)
    warmUp(applicationContext)
}

private fun warmUp(applicationContext: ConfigurableApplicationContext) {
    val bankService = applicationContext.getBean(BankService::class.java)
    Tenant.entries.forEach {
        try {
            MultiTenantContext.setCurrentTenant(it)
            logger.info("Retrieving balance and transactions for tenant $it")
            bankService.getBalance()
            bankService.getTransactions(1, 0)
        } finally {
            MultiTenantContext.clear()
        }
    }

    logger.info("Retrieved balance and transactions to warm up cache")
}
