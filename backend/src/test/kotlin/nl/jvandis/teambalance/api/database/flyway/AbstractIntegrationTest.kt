package nl.jvandis.teambalance.api.database.flyway

import nl.jvandis.jooq.support.migrations.LiquibaseSupport
import nl.jvandis.teambalance.loggerFor
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager

@Testcontainers
@Transactional
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
@AutoConfigureMockMvc
class AbstractIntegrationTest {

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
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

            ctx = using(connection, SQLDialect.POSTGRES)

            connection.createStatement().use { s ->
                log.info("Setting up database")

                log.info("Migrating data with liquibase")
                LiquibaseSupport.migrate(
                    connection,
                    "db.changelog-master.xml",
                    "${System.getProperty("user.dir")}/src/main/resources/db/changelog/",
                    "public"
                )
                log.info("Finished setup")
            }
        }
    }

    @BeforeEach
    fun `open a transaction`() {
//        ctx.transaction{x -> DefaultTransactionProvider(x.connectionProvider())}
    }

    @AfterEach
    fun `close a transaction, rolling back any changes`() {
    }

    companion object {

        private var log = loggerFor()
        lateinit var connection: Connection
        lateinit var ctx: DSLContext

        @Container
        val postgresqlContainer = PostgreSQLContainer("postgres:11.18-bullseye").apply {
            withDatabaseName("teambalance")
            withUsername("teambalance")
            withPassword("teambalance")
        }

        @JvmStatic
        @BeforeAll
        fun `get postgres`() {
        }
    }
}

internal class IntegrationTestConfig
