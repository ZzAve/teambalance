package nl.jvandis.teambalance

import nl.jvandis.jooq.support.migrations.LiquibaseSupport
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager

@Testcontainers
@Transactional("transactionManager")
@SpringBootTest
@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
@AutoConfigureMockMvc
class AbstractIntegrationTest {

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            // detect running db
            log.info("Running initializer")

            // if not start testcontainers thing
            postgresqlContainer.start()
            runLiquibaseMigrations()

            TestPropertyValues.of(
                "spring.datasource.username=${postgresqlContainer.username}",
                "spring.datasource.password=${postgresqlContainer.password}",
                "spring.datasource.url=${postgresqlContainer.jdbcUrl}"
            ).applyTo(configurableApplicationContext.environment)
        }

        private fun runLiquibaseMigrations() {
            connection = DriverManager.getConnection(
                postgresqlContainer.jdbcUrl,
                postgresqlContainer.username,
                postgresqlContainer.password
            )

            // Note: naive way of creating a DSLContext that is NOT tenant / schema aware.
            // FIXME
            context = using(connection, SQLDialect.POSTGRES)

            connection.createStatement().use { s ->
                Tenant.entries.forEach { tenant ->
                    log.info("Creating schema ${tenant.name.lowercase()}")
                    val execute =
                        s.execute("CREATE SCHEMA IF NOT EXISTS ${tenant.name.lowercase()} AUTHORIZATION ${postgresqlContainer.username};")
                    log.info("Creating successful? {}", execute)
                }
            }
            Tenant.entries.forEach { tenant ->
                connection.createStatement().use { s ->
                    log.info("Migrating data with liquibase")
                    LiquibaseSupport.migrate(
                        connection,
                        "db.changelog-master.xml",
                        "${System.getProperty("user.dir")}/../backend/src/main/resources/db/changelog/", // TODO: change me to classpath resource
                        tenant.name.lowercase()
                    )
                    log.info("Finished setup")
                }
            }
        }
    }


    @BeforeEach
    fun `open a transaction`() {
        log.info("Before each")
    }

    @AfterEach
    fun `close a transaction, rolling back any changes`() {
        log.info("After each")
    }

    companion object {

        private var log = loggerFor()
        lateinit var connection: Connection

        /**
         * WARNING: This context is tenant specific; be advised
         */
        lateinit var context: DSLContext

        @Container
        val postgresqlContainer = PostgreSQLContainer("postgres:11.18-bullseye").apply {
            withDatabaseName("teambalance")
            withUsername("teambalance")
            withPassword("teambalance")
            withUrlParam("loggerLevel", "DEBUG")
            waitingFor(
                Wait.forLogMessage(".*ready to accept connections.*\\n", 1)
            ).withConnectTimeoutSeconds(20)
        }

        @JvmStatic
        @BeforeAll
        fun `setup mockmvc default header`() {
        }
    }
}


internal class IntegrationTestConfig
