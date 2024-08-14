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

internal fun interface LoggingContext {
    fun logger(): Logger
}

internal inline val <reified T> T.log: Logger
    get() = LoggerFactory.getLogger(T::class.java)

fun main(args: Array<String>) {
    val applicationContext = SpringApplication.run(Application::class.java, *args)

    with(
        LoggingContext {
            loggerFor("nl.jvandis.teambalance.init")
        },
    ) {
        warmUp(applicationContext)
    }
}

context(LoggingContext)
private fun warmUp(applicationContext: ConfigurableApplicationContext) {
    Tenant.entries.forEach {
        log.info("Retrieving balance and transactions for $it")
        MultiTenantContext.setCurrentTenant(it)
        applicationContext.getBean(BankService::class.java).getBalance()
        applicationContext.getBean(BankService::class.java).getTransactions(1, 0)
        MultiTenantContext.clear()
    }

    log.info("Retrieved balance and transactions to warm up cache")
}
